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
import java.util.List;

import javax.swing.Icon;
import javax.swing.JComponent;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Function;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class OverrideTypeCollector implements LineMarkerCollector
{
	@RequiredReadAction
	@Override
	public void collect(PsiElement psiElement, @NotNull Collection<LineMarkerInfo> lineMarkerInfos)
	{
		CSharpTypeDeclaration parent = CSharpLineMarkerUtil.getNameIdentifierAs(psiElement, CSharpTypeDeclaration.class);
		if(parent != null)
		{
			if(hasChild(parent))
			{
				final Icon icon = parent.isInterface() ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.OverridenMethod;
				LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), icon, Pass.UPDATE_OVERRIDEN_MARKERS, new Function<PsiElement,
						String>()

				{
					@Override
					public String fun(PsiElement element)
					{
						return "Searching for overriding";
					}
				}, new GutterIconNavigationHandler<PsiElement>()
				{
					@Override
					public void navigate(MouseEvent mouseEvent, PsiElement element)
					{
						final DotNetTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						assert typeDeclaration != null;
						final CommonProcessors.CollectProcessor<DotNetTypeDeclaration> collectProcessor = new CommonProcessors.CollectProcessor<DotNetTypeDeclaration>();
						if(!ProgressManager.getInstance().runProcessWithProgressSynchronously(new Runnable()
						{
							@Override
							public void run()
							{
								TypeInheritorsSearch.search(typeDeclaration, true).forEach(collectProcessor);
							}
						}, "Searching for overriding", true, typeDeclaration.getProject(), (JComponent) mouseEvent.getComponent()))
						{
							return;
						}

						Collection<DotNetTypeDeclaration> results = collectProcessor.getResults();

						List<NavigatablePsiElement> typeDeclarations = new ArrayList<NavigatablePsiElement>(results.size());
						for(DotNetTypeDeclaration result : results)
						{
							PsiElement fun = CSharpTransformer.INSTANCE.fun(result);
							if(fun instanceof NavigatablePsiElement)
							{
								typeDeclarations.add((NavigatablePsiElement) fun);
							}
						}

						PsiElementListNavigator.openTargets(mouseEvent, typeDeclarations.toArray(new NavigatablePsiElement[0]), "Navigate to inheritors", "Navigate to inheritors",
								new DefaultPsiElementCellRenderer());
					}
				}, GutterIconRenderer.Alignment.RIGHT
				);
				lineMarkerInfos.add(lineMarkerInfo);
			}
		}
	}

	private static boolean hasChild(final CSharpTypeDeclaration type)
	{
		return TypeInheritorsSearch.search(type, false).findFirst() != null;
	}
}
