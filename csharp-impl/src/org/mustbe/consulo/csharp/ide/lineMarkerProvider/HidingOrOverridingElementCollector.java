/*
 * Copyright 2013-2014 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mustbe.consulo.csharp.ide.lineMarkerProvider;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ConstantFunction;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class HidingOrOverridingElementCollector implements LineMarkerCollector
{
	private static class OurHandler implements GutterIconNavigationHandler<PsiElement>
	{
		public static final OurHandler INSTANCE = new OurHandler();

		@Override
		public void navigate(MouseEvent mouseEvent, PsiElement element)
		{
			PsiElement parent = element.getParent();
			if(!(parent instanceof DotNetVirtualImplementOwner) || !isAllowForOverride(parent))
			{
				return;
			}

			PsiElement maybeTypeDeclaration = parent.getParent();
			if(!(maybeTypeDeclaration instanceof DotNetTypeDeclaration))
			{
				return;
			}

			Collection<DotNetVirtualImplementOwner> members = findOverridingMembers((DotNetTypeDeclaration) maybeTypeDeclaration,
					(DotNetVirtualImplementOwner) parent);

			if(members.isEmpty())
			{
				return;
			}

			if(members.size() == 1)
			{
				DotNetVirtualImplementOwner firstItem = ContainerUtil.getFirstItem(members);
				if(firstItem instanceof Navigatable)
				{
					((Navigatable) firstItem).navigate(true);
				}
			}
			else
			{
				PsiElement[] elements = members.toArray(new PsiElement[members.size()]);

				JBPopup popup = NavigationUtil.getPsiElementPopup(elements, "Open elements (" + elements.length + " items)");
				popup.show(new RelativePoint(mouseEvent));
			}
		}
	}

	@Override
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		PsiElement parent = psiElement.getParent();
		IElementType elementType = psiElement.getNode().getElementType();
		if((elementType == CSharpTokens.IDENTIFIER || elementType == CSharpTokens.THIS_KEYWORD) && isAllowForOverride(parent))
		{
			PsiElement parentParent = parent.getParent();
			if(!(parentParent instanceof DotNetTypeDeclaration))
			{
				return;
			}

			DotNetVirtualImplementOwner virtualImplementOwner = (DotNetVirtualImplementOwner) parent;

			Collection<DotNetVirtualImplementOwner> overrideElements = findOverridingMembers((DotNetTypeDeclaration) parentParent,
					virtualImplementOwner);

			if(overrideElements.isEmpty())
			{
				return;
			}

			Icon icon = null;
			if(virtualImplementOwner.getTypeForImplement() != null)
			{
				icon = CSharpIcons.Gutter.HidingMethod;
			}
			else
			{
				icon = AllIcons.Gutter.ImplementingMethod;
				for(DotNetVirtualImplementOwner overrideElement : overrideElements)
				{
					if(!((DotNetModifierListOwner) overrideElement).hasModifier(DotNetModifier.ABSTRACT))
					{
						icon = AllIcons.Gutter.OverridingMethod;
						break;
					}
				}
			}

			val lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), icon, Pass.UPDATE_OVERRIDEN_MARKERS,
					new ConstantFunction<PsiElement, String>("Searching for overriding"), OurHandler.INSTANCE, GutterIconRenderer.Alignment.LEFT
			);

			lineMarkerInfos.add(lineMarkerInfo);
		}
	}

	private static boolean isAllowForOverride(PsiElement parent)
	{
		if(parent instanceof CSharpMethodDeclaration &&
				!((CSharpMethodDeclaration) parent).isDelegate() && !((CSharpMethodDeclaration) parent).hasModifier(DotNetModifier.STATIC))
		{
			return true;
		}
		return parent instanceof DotNetVirtualImplementOwner;
	}

	@NotNull
	private static Collection<DotNetVirtualImplementOwner> findOverridingMembers(final DotNetTypeDeclaration owner,
			final DotNetVirtualImplementOwner target)
	{
		CommonProcessors.CollectProcessor<DotNetVirtualImplementOwner> overrideProcessor = new CommonProcessors
				.CollectProcessor<DotNetVirtualImplementOwner>();

		MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(owner, new ExecuteTarget[]{
				ExecuteTarget.MEMBER,
				ExecuteTarget.ELEMENT_GROUP
		}, overrideProcessor);

		ResolveState state = ResolveState.initial();
		if(target instanceof CSharpArrayMethodDeclaration)
		{
			state = state.put(CSharpResolveUtil.SELECTOR, StaticResolveSelectors.INDEX_METHOD_GROUP);
		}
		else
		{
			String name = ((PsiNamedElement) target).getName();
			if(name == null)
			{
				return Collections.emptyList();
			}
			state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(name));
		}

		CSharpResolveUtil.walkChildren(processor, owner, false, true, state);

		return overrideProcessor.getResults();
	}
}
