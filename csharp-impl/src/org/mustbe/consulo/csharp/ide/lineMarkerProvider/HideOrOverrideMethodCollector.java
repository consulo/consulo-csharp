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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementCompareUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MemberResolveScopeProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.MemberByNameSelector;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
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
public class HideOrOverrideMethodCollector implements LineMarkerCollector
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

	@Override
	public void collect(
			PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		PsiElement parent = psiElement.getParent();
		if(parent instanceof CSharpMethodDeclaration &&
				psiElement.getNode().getElementType() == CSharpTokens.IDENTIFIER &&
				!((CSharpMethodDeclaration) parent).isDelegate() && !((CSharpMethodDeclaration) parent).hasModifier(DotNetModifier.STATIC))
		{
			PsiElement methodParent = parent.getParent();
			if(!(methodParent instanceof CSharpTypeDeclaration))
			{
				return;
			}

			MultiMap<Category, CSharpMethodDeclaration> parentMethods = split((CSharpTypeDeclaration) methodParent,
					(CSharpMethodDeclaration) parent);


			for(Map.Entry<Category, Collection<CSharpMethodDeclaration>> entry : parentMethods.entrySet())
			{
				val key = entry.getKey();

				val lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), key.icon(), Pass.UPDATE_OVERRIDEN_MARKERS,
						new Function<PsiElement, String>()

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
						CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element.getParent();
						CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) methodDeclaration.getParent();

						MultiMap<Category, CSharpMethodDeclaration> split = split(typeDeclaration, methodDeclaration);

						Collection<CSharpMethodDeclaration> declarations = split.get(key);
						if(declarations.isEmpty())
						{
							return;
						}

						CSharpMethodDeclaration[] inheritors = declarations.toArray(CSharpMethodDeclaration.EMPTY_ARRAY);
						if(inheritors.length == 1)
						{
							((Navigatable)inheritors[0]).navigate(true);
							return;
						}

						JBPopup popup = NavigationUtil.getPsiElementPopup(inheritors, "Open methods (" + inheritors.length + " items)");
						popup.show(new RelativePoint(mouseEvent));
					}
				}, GutterIconRenderer.Alignment.LEFT
				);
				lineMarkerInfos.add(lineMarkerInfo);
			}
		}
	}

	@NotNull
	private static MultiMap<Category, CSharpMethodDeclaration> split(final CSharpTypeDeclaration owner, final CSharpMethodDeclaration target)
	{
		List<CSharpMethodDeclaration> parentMethods = findParentMethods(owner, target);
		if(parentMethods.isEmpty())
		{
			return MultiMap.emptyInstance();
		}


		MultiMap<Category, CSharpMethodDeclaration> map = new MultiMap<Category, CSharpMethodDeclaration>();
		for(CSharpMethodDeclaration parentMethod : parentMethods)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) parentMethod.getParent();
			if(parent == null)
			{
				continue;
			}
			if(parent.isInterface())
			{
				map.putValue(Category.implement, parentMethod);
			}
			else
			{
				map.putValue(target.hasModifier(CSharpModifier.OVERRIDE) ? Category.override : Category.hide, parentMethod);
			}
		}
		return map;
	}

	private static List<CSharpMethodDeclaration> findParentMethods(final CSharpTypeDeclaration owner, final CSharpMethodDeclaration target)
	{
		final List<CSharpMethodDeclaration> parents = new SmartList<CSharpMethodDeclaration>();

		MemberResolveScopeProcessor processor = new MemberResolveScopeProcessor(owner.getResolveScope(), ResolveResult.EMPTY_ARRAY,
				new ExecuteTarget[] {ExecuteTarget.ELEMENT_GROUP});

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.SELECTOR, new MemberByNameSelector(target.getName()));

		CSharpResolveUtil.walkChildren(processor, owner, false, true, state);

		PsiElement[] psiElements = processor.toPsiElements();
		for(PsiElement psiElement : psiElements)
		{
			CSharpElementGroup<?> elementGroup = (CSharpElementGroup<?>) psiElement;

			for(PsiElement element : elementGroup.getElements())
			{
				if(element instanceof CSharpMethodDeclaration)
				{
					if(element.getParent() == owner)
					{
						continue;
					}
					if(CSharpElementCompareUtil.isEqual(element, target, owner))
					{
						parents.add((CSharpMethodDeclaration) element);
					}
				}
			}
		}

		return parents;
	}
}
