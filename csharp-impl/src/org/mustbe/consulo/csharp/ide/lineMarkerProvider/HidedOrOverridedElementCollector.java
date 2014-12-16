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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.StaticResolveSelectors;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.psi.search.searches.ClassInheritorsSearch;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ConstantFunction;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 15.12.14
 */
public class HidedOrOverridedElementCollector implements LineMarkerCollector
{
	private static class OurHandler implements GutterIconNavigationHandler<PsiElement>
	{
		public static final OurHandler INSTANCE = new OurHandler();

		@Override
		public void navigate(MouseEvent mouseEvent, PsiElement element)
		{
			PsiElement parent = element.getParent();
			if(!(parent instanceof DotNetVirtualImplementOwner) || !OverrideUtil.isAllowForOverride(parent))
			{
				return;
			}

			PsiElement maybeTypeDeclaration = parent.getParent();
			if(!(maybeTypeDeclaration instanceof DotNetTypeDeclaration))
			{
				return;
			}

			Collection<DotNetVirtualImplementOwner> members = findOverridedMembers((DotNetTypeDeclaration) maybeTypeDeclaration,
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

				JBPopup popup = NavigationUtil.getPsiElementPopup(elements, new ElementGutterRender(), "Open elements (" + elements.length + " " +
						"items)" +
						"");
				popup.show(new RelativePoint(mouseEvent));
			}
		}
	}

	@Override
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		PsiElement parent = psiElement.getParent();
		IElementType elementType = psiElement.getNode().getElementType();
		if((elementType == CSharpTokens.IDENTIFIER || elementType == CSharpTokens.THIS_KEYWORD) && OverrideUtil.isAllowForOverride(parent))
		{
			PsiElement parentParent = parent.getParent();
			if(!(parentParent instanceof DotNetTypeDeclaration))
			{
				return;
			}

			DotNetVirtualImplementOwner virtualImplementOwner = (DotNetVirtualImplementOwner) parent;

			Collection<DotNetVirtualImplementOwner> overrideElements = findOverridedMembers((DotNetTypeDeclaration) parentParent,
					virtualImplementOwner);

			if(overrideElements.isEmpty())
			{
				return;
			}

			Icon icon = CSharpIcons.Gutter.HidedMethod;
			for(DotNetVirtualImplementOwner overrideElement : overrideElements)
			{
				if(overrideElement.getTypeForImplement() == null)
				{
					icon = null;
					break;
				}
			}

			if(icon == null)
			{
				boolean allAbstract = true;
				for(DotNetVirtualImplementOwner overrideElement : overrideElements)
				{
					if(!(overrideElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) overrideElement).hasModifier
							(DotNetModifier.ABSTRACT)))
					{
						allAbstract = false;
						break;
					}
				}

				boolean abstractMe = virtualImplementOwner instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) virtualImplementOwner)
						.hasModifier(DotNetModifier.ABSTRACT);

				if(allAbstract && abstractMe)
				{
					icon = AllIcons.Gutter.OverridenMethod;
				}
				else if(abstractMe)
				{
					icon = AllIcons.Gutter.ImplementedMethod;
				}
				else
				{
					icon = AllIcons.Gutter.OverridenMethod;
				}
			}
			val lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), icon, Pass.UPDATE_OVERRIDEN_MARKERS,
					new ConstantFunction<PsiElement, String>("Searching for overrided"), OurHandler.INSTANCE, GutterIconRenderer.Alignment.LEFT);

			lineMarkerInfos.add(lineMarkerInfo);
		}
	}


	@NotNull
	private static Collection<DotNetVirtualImplementOwner> findOverridedMembers(final DotNetTypeDeclaration owner,
			final DotNetVirtualImplementOwner target)
	{
		final CSharpResolveSelector selector;
		if(target instanceof CSharpArrayMethodDeclaration)
		{
			selector = StaticResolveSelectors.INDEX_METHOD_GROUP;
		}
		else
		{
			String name = ((PsiNamedElement) target).getName();
			if(name != null)
			{
				selector = new MemberByNameSelector(name);
			}
			else
			{
				selector = null;
			}
		}

		if(selector == null)
		{
			return Collections.emptyList();
		}
		final GlobalSearchScope resolveScope = target.getResolveScope();

		final List<DotNetVirtualImplementOwner> list = new ArrayList<DotNetVirtualImplementOwner>();
		Query<DotNetTypeDeclaration> search = ClassInheritorsSearch.search(owner, true, CSharpTransform.INSTANCE);
		search.forEach(new Processor<DotNetTypeDeclaration>()
		{
			@Override
			public boolean process(DotNetTypeDeclaration typeDeclaration)
			{
				CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, resolveScope, typeDeclaration);

				PsiElement[] elements = selector.doSelectElement(context, false);
				for(PsiElement element : CSharpResolveUtil.mergeGroupsToIterable(elements))
				{
					if(CSharpElementCompareUtil.isEqual(element, target, CSharpElementCompareUtil.CHECK_RETURN_TYPE, target))
					{
						list.add((DotNetVirtualImplementOwner) element);
					}
				}
				return true;
			}
		});

		return list;
	}
}
