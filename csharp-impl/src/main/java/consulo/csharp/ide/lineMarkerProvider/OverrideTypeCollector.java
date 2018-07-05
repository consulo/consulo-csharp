/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.ide.lineMarkerProvider;

import java.awt.event.MouseEvent;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.swing.JComponent;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Consumer;
import com.intellij.util.FunctionUtil;
import com.intellij.util.Functions;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.msil.CSharpTransformer;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import consulo.ui.image.Image;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class OverrideTypeCollector implements LineMarkerCollector
{
	@RequiredReadAction
	@Override
	public void collect(PsiElement psiElement, @Nonnull Consumer<LineMarkerInfo> consumer)
	{
		CSharpTypeDeclaration parent = CSharpLineMarkerUtil.getNameIdentifierAs(psiElement, CSharpTypeDeclaration.class);
		if(parent != null)
		{
			if(hasChild(parent))
			{
				Image icon = parent.isInterface() ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.OverridenMethod;
				LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(psiElement, psiElement.getTextRange(), icon, Pass.LINE_MARKERS, FunctionUtil.constant("Searching for overriding"),
						new GutterIconNavigationHandler<PsiElement>()
				{
					@Override
					@RequiredDispatchThread
					public void navigate(MouseEvent mouseEvent, PsiElement element)
					{
						final DotNetTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
						assert typeDeclaration != null;
						final CommonProcessors.CollectProcessor<DotNetTypeDeclaration> collectProcessor = new CommonProcessors.CollectProcessor<>();
						if(!ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> TypeInheritorsSearch.search(typeDeclaration, true).forEach(collectProcessor), "Searching for " +
								"overriding", true, typeDeclaration.getProject(), (JComponent) mouseEvent.getComponent()))
						{
							return;
						}

						Collection<DotNetTypeDeclaration> results = collectProcessor.getResults();

						CSharpLineMarkerUtil.openTargets(ContainerUtil.map(results, CSharpTransformer.INSTANCE), mouseEvent, "Navigate to inheritors", Functions.<PsiElement, PsiElement>identity());
					}
				}, GutterIconRenderer.Alignment.RIGHT);
				consumer.consume(lineMarkerInfo);
			}
		}
	}

	private static boolean hasChild(final CSharpTypeDeclaration type)
	{
		return TypeInheritorsSearch.search(type, false).findFirst() != null;
	}
}
