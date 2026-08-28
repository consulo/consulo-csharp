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
import consulo.application.Application;
import consulo.application.ReadAction;
import consulo.application.util.function.CommonProcessors;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.csharp.lang.impl.psi.msil.CSharpTransformer;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.search.searches.TypeInheritorsSearch;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.ui.navigation.PsiTargetNavigationService;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.event.ComponentEvent;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;
import consulo.util.concurrent.coroutine.CoroutineStep;
import consulo.util.concurrent.coroutine.step.CodeExecution;
import consulo.util.lang.function.Functions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class OverrideTypeCollector implements LineMarkerCollector {
    public static final OverrideTypeCollector INSTANCE = new OverrideTypeCollector();

    @RequiredReadAction
    @Override
    public void collect(PsiElement psiElement, Consumer<LineMarkerInfo> consumer) {
        CSharpTypeDeclaration parent = CSharpLineMarkerUtil.getNameIdentifierAs(psiElement, CSharpTypeDeclaration.class);
        if (parent != null) {
            if (hasChild(parent)) {
                Image icon = parent.isInterface() ? AllIcons.Gutter.ImplementedMethod : AllIcons.Gutter.OverridenMethod;
                LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<>(psiElement,
                    psiElement.getTextRange(),
                    icon,
                    Pass.LINE_MARKERS,
                    e -> "Searching for overriding",
                    this::navigate,
                    GutterIconRenderer.Alignment.RIGHT
                );
                consumer.accept(lineMarkerInfo);
            }
        }
    }

    @Override
    @RequiredUIAccess
    public void navigate(ComponentEvent<?> event, PsiElement element) {
        DotNetTypeDeclaration typeDeclaration = element instanceof DotNetTypeDeclaration t
            ? t
            : CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
        if (typeDeclaration == null) {
            return;
        }

        PsiMappedPresentationProvider provider = new PsiMappedPresentationProvider(Functions.identity());

        CoroutineStep<Void, Collection<PsiElement>> search = CodeExecution.supply(() -> {
            CommonProcessors.CollectProcessor<DotNetTypeDeclaration> collectProcessor = new CommonProcessors.CollectProcessor<>();
            TypeInheritorsSearch.search(typeDeclaration, true).forEach(collectProcessor);

            return ReadAction.compute(() -> {
                List<PsiElement> results = new ArrayList<>(ContainerUtil.map(collectProcessor.getResults(), CSharpTransformer.INSTANCE));
                results.sort(provider.comparator());
                return results;
            });
        });

        Application.get().getInstance(PsiTargetNavigationService.class)
            .newNavigator(search)
            .presentationProvider(provider)
            .title(LocalizeValue.localizeTODO("Navigate to inheritors"))
            .findUsagesTitle(LocalizeValue.localizeTODO("Navigate to inheritors"))
            .navigate(event, typeDeclaration.getProject());
    }

    private static boolean hasChild(final CSharpTypeDeclaration type) {
        return TypeInheritorsSearch.search(type, false).findFirst() != null;
    }
}
