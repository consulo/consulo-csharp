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

package consulo.csharp.impl.ide.lineMarkerProvider;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.AllIcons;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.CommonProcessors;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.csharp.lang.impl.psi.msil.CSharpTransformer;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.psi.PsiElement;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.function.Functions;

import jakarta.annotation.Nonnull;
import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.function.Consumer;

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
				LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(psiElement, psiElement.getTextRange(), icon, Pass.LINE_MARKERS, e -> "Searching for overriding",
						new GutterIconNavigationHandler<PsiElement>()
				{
					@Override
					@RequiredUIAccess
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
				consumer.accept(lineMarkerInfo);
			}
		}
	}

	private static boolean hasChild(final CSharpTypeDeclaration type)
	{
		return TypeInheritorsSearch.search(type, false).findFirst() != null;
	}
}
