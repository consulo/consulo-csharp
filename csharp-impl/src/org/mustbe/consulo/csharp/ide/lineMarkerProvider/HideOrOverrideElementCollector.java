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
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CompletionResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
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
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import com.intellij.util.containers.MultiMap;
import lombok.val;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class HideOrOverrideElementCollector implements LineMarkerCollector
{
	public static enum Category
	{
		implement
				{
					@Override
					public Icon icon()
					{
						return AllIcons.Gutter.ImplementingMethod;
					}
				},
		override
				{
					@Override
					public Icon icon()
					{
						return AllIcons.Gutter.OverridingMethod;
					}
				},
		hide
				{
					@Override
					public Icon icon()
					{
						return CSharpIcons.Gutter.HidingMethod;
					}
				};


		public abstract Icon icon();
	}

	private boolean isAllowForOverride(PsiElement parent)
	{
		if(parent instanceof CSharpMethodDeclaration &&
				!((CSharpMethodDeclaration) parent).isDelegate() && !((CSharpMethodDeclaration) parent).hasModifier(DotNetModifier.STATIC))
		{
			return true;
		}
		if(parent instanceof CSharpPropertyDeclaration || parent instanceof CSharpArrayMethodDeclaration || parent instanceof CSharpEventDeclaration)
		{
			return true;
		}
		return false;
	}

	@Override
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		PsiElement parent = psiElement.getParent();
		if(psiElement.getNode().getElementType() == CSharpTokens.IDENTIFIER && isAllowForOverride(parent))
		{
			PsiElement parentParent = parent.getParent();
			if(!(parentParent instanceof CSharpTypeDeclaration))
			{
				return;
			}

			MultiMap<Category, DotNetModifierListOwner> parentMethods = split((CSharpTypeDeclaration) parentParent,
					(DotNetModifierListOwner) parent);


			for(Map.Entry<Category, Collection<DotNetModifierListOwner>> entry : parentMethods.entrySet())
			{
				val key = entry.getKey();

				val lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), key.icon(),
						Pass.UPDATE_OVERRIDEN_MARKERS, new Function<PsiElement, String>()

				{
					@Override
					public String fun(PsiElement element)
					{
						switch(key)
						{
							case implement:
								return "Search for implemented methods";
							case override:
								return "Search for overriding methods";
							case hide:
								return "Search for hiding methods";
						}
						throw new IllegalArgumentException();
					}
				}, new GutterIconNavigationHandler<PsiElement>()
				{
					@Override
					public void navigate(MouseEvent mouseEvent, PsiElement element)
					{
						DotNetModifierListOwner someElement = (DotNetModifierListOwner) element.getParent();
						CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) someElement.getParent();

						MultiMap<Category, DotNetModifierListOwner> split = split(typeDeclaration, someElement);

						Collection<DotNetModifierListOwner> declarations = split.get(key);
						if(declarations.isEmpty())
						{
							return;
						}

						PsiElement[] inheritors = declarations.toArray(PsiElement.EMPTY_ARRAY);
						if(inheritors.length == 1)
						{
							((Navigatable) inheritors[0]).navigate(true);
							return;
						}

						JBPopup popup = NavigationUtil.getPsiElementPopup(inheritors, "Open elements (" + inheritors.length + " items)");
						popup.show(new RelativePoint(mouseEvent));
					}
				}, GutterIconRenderer.Alignment.LEFT
				);
				lineMarkerInfos.add(lineMarkerInfo);
			}
		}
	}

	@NotNull
	private static MultiMap<Category, DotNetModifierListOwner> split(final CSharpTypeDeclaration owner, final DotNetModifierListOwner target)
	{
		List<DotNetModifierListOwner> parentElements = findParentElements(owner, target);
		if(parentElements.isEmpty())
		{
			return MultiMap.emptyInstance();
		}


		MultiMap<Category, DotNetModifierListOwner> map = new MultiMap<Category, DotNetModifierListOwner>();
		for(DotNetModifierListOwner parentElement : parentElements)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) parentElement.getParent();
			if(parent == null)
			{
				continue;
			}
			if(parent.isInterface())
			{
				map.putValue(Category.implement, parentElement);
			}
			else
			{
				map.putValue(target.hasModifier(CSharpModifier.OVERRIDE) ? Category.override : Category.hide, parentElement);
			}
		}
		return map;
	}

	private static List<DotNetModifierListOwner> findParentElements(final CSharpTypeDeclaration owner, final PsiElement target)
	{
		final List<DotNetModifierListOwner> parents = new SmartList<DotNetModifierListOwner>();

		CompletionResolveScopeProcessor processor = new CompletionResolveScopeProcessor(owner.getResolveScope(), ResolveResult.EMPTY_ARRAY,
				new ExecuteTarget[]{ExecuteTarget.MEMBER});

		ResolveState state = ResolveState.initial();

		CSharpResolveUtil.walkChildren(processor, owner, false, true, state);

		PsiElement[] psiElements = processor.toPsiElements();
		for(PsiElement psiElement : psiElements)
		{
			if(psiElement.getParent() == owner)
			{
				continue;
			}
			if(CSharpElementCompareUtil.isEqual(psiElement, target, owner))
			{
				parents.add((DotNetModifierListOwner) psiElement);
			}
		}

		return parents;
	}
}
