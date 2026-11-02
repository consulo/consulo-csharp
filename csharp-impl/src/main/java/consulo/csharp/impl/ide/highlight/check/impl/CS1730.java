/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import org.jspecify.annotations.Nullable;

/**
 * Assembly and module attributes must precede all other elements defined in a file except using clauses and extern alias
 * declarations - and are not of a namespace.
 *
 * @author VISTALL
 */
public class CS1730 extends CompilerCheck<CSharpAttributeList> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAttributeList element) {
        DotNetAttributeTargetType targetType = element.getTargetType();
        if (targetType != DotNetAttributeTargetType.ASSEMBLY && targetType != DotNetAttributeTargetType.MODULE) {
            return null;
        }

        PsiElement parent = element.getParent();
        boolean misplaced = parent instanceof CSharpNamespaceDeclaration;
        if (!misplaced && parent instanceof PsiFile) {
            for (PsiElement prev = element.getPrevSibling(); prev != null; prev = prev.getPrevSibling()) {
                // a type, namespace or member - using directives may precede
                if (prev instanceof DotNetNamedElement) {
                    misplaced = true;
                    break;
                }
            }
        }

        if (!misplaced) {
            return null;
        }

        ASTNode target = element.getNode().findChildByType(CSharpTokenSets.GLOBAL_ATTRIBUTE_TARGETS);
        return newBuilder(target == null ? element : target.getPsi());
    }
}
