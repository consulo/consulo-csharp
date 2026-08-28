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
import consulo.application.Application;
import consulo.codeEditor.markup.GutterIconRenderer;
import consulo.component.util.Iconable;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.language.editor.Pass;
import consulo.language.editor.gutter.GutterIconNavigationHandler;
import consulo.language.editor.gutter.LineMarkerInfo;
import consulo.language.editor.ui.PsiElementListCellRenderer;
import consulo.language.editor.ui.navigation.PsiTargetNavigationService;
import consulo.language.editor.ui.navigation.PsiTargetPresentationFactory;
import consulo.language.editor.ui.navigation.TargetPresentationProvider;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.SymbolPresentationUtil;
import consulo.localize.LocalizeValue;
import consulo.navigation.TargetPresentation;
import consulo.navigation.TargetPresentationBuilder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.event.ComponentEvent;
import consulo.virtualFileSystem.VirtualFile;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public class PartialTypeCollector implements LineMarkerCollector {
    public static class OurRender extends PsiElementListCellRenderer<PsiElement> {
        @Override
        public String getElementText(PsiElement element) {
            VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
            return virtualFile == null ? SymbolPresentationUtil.getSymbolPresentableText(element) : virtualFile.getName();
        }

        @Nullable
        @Override
        protected String getContainerText(PsiElement element, String name) {
            VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
            if (virtualFile == null) {
                return SymbolPresentationUtil.getSymbolContainerText(element);
            }
            else {
                return "(" + virtualFile.getPath() + ")";
            }
        }

        @Override
        protected int getIconFlags() {
            return Iconable.ICON_FLAG_VISIBILITY;
        }
    }

    /**
     * What {@link OurRender} draws, as data built once under a read lock - a partial part named by the file
     * it lives in. The render itself stays only for {@code GotoDeclarationHandlerEx}, which still speaks in
     * renderers.
     */
    public static class OurPresentationProvider implements TargetPresentationProvider<PsiElement> {
        @Override
        @RequiredReadAction
        public TargetPresentation getPresentation(PsiElement element) {
            TargetPresentationBuilder builder = Application.get().getInstance(PsiTargetPresentationFactory.class).presentationBuilder(element);
            builder = builder.withIcon(IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY));

            VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
            if (virtualFile != null) {
                builder = builder.withPresentableText(LocalizeValue.of(virtualFile.getName()));
                builder = builder.withContainerText(LocalizeValue.of("(" + virtualFile.getPath() + ")"));
            }
            return builder.build();
        }
    }

    @RequiredReadAction
    @Override
    public void collect(PsiElement psiElement, Consumer<LineMarkerInfo> consumer) {
        CSharpTypeDeclaration parent = CSharpLineMarkerUtil.getNameIdentifierAs(psiElement, CSharpTypeDeclaration.class);
        if (parent != null) {
            if (!parent.hasModifier(CSharpModifier.PARTIAL)) {
                return;
            }

            CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(parent);
            if (compositeType == null) {
                return;
            }

            LineMarkerInfo<PsiElement> lineMarkerInfo = new LineMarkerInfo<PsiElement>(psiElement, psiElement.getTextRange(), PlatformIconGroup.gutterFold(), Pass.LINE_MARKERS,
                element -> "Navigate to partial types", new GutterIconNavigationHandler<PsiElement>() {
                @Override
                @RequiredUIAccess
                public void navigate(ComponentEvent<?> event, PsiElement element) {
                    Application.get().getInstance(PsiTargetNavigationService.class)
                        .<PsiElement>newNavigator(() ->
                        {
                            CSharpTypeDeclaration typeDeclaration = CSharpLineMarkerUtil.getNameIdentifierAs(element, CSharpTypeDeclaration.class);
                            if (typeDeclaration == null) {
                                return List.of();
                            }

                            CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType(typeDeclaration);
                            if (compositeType == null) {
                                return List.of();
                            }

                            return List.<PsiElement>of(compositeType.getTypeDeclarations());
                        })
                        .presentationProvider(new OurPresentationProvider())
                        .title(LocalizeValue.localizeTODO("Navigate to partial types"))
                        .findUsagesTitle(LocalizeValue.localizeTODO("Navigate to partial types"))
                        .navigate(event, element.getProject());
                }
            }, GutterIconRenderer.Alignment.CENTER
            );
            consumer.accept(lineMarkerInfo);
        }
    }
}
