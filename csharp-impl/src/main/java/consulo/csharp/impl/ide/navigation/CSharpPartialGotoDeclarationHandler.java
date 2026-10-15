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
package consulo.csharp.impl.ide.navigation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.lineMarkerProvider.PartialTypeCollector;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightConstructorDeclarationBuilder;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.ASTNode;
import consulo.language.editor.ui.PsiElementListCellRenderer;
import consulo.language.editor.ui.navigation.GotoDeclarationHandlerEx;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 2015-05-02
 */
@ExtensionImpl
public class CSharpPartialGotoDeclarationHandler implements GotoDeclarationHandlerEx {
    @Override
    @RequiredReadAction
    public PsiElement @Nullable [] getGotoDeclarationTargets(PsiElement sourceElement, int offset, Editor editor) {
        ASTNode node = sourceElement == null ? null : sourceElement.getNode();
        if (node != null && node.getElementType() == CSharpTokens.IDENTIFIER) {
            if (sourceElement.getParent() instanceof CSharpReferenceExpression ref) {
                PsiElement resolvedElement = ref.resolve();
                if (resolvedElement instanceof CSharpCompositeTypeDeclaration compositeTypeDecl) {
                    return compositeTypeDecl.getTypeDeclarations();
                }
                else if (resolvedElement instanceof CSharpLightConstructorDeclarationBuilder lightConstructorDeclBuilder) {
                    if (lightConstructorDeclBuilder.getParent() instanceof CSharpCompositeTypeDeclaration compositeTypeDecl) {
                        return compositeTypeDecl.getTypeDeclarations();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public PsiElementListCellRenderer<PsiElement> createRenderer(PsiElement[] elements) {
        return new PartialTypeCollector.OurRender();
    }
}
