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
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Cannot use local variable before it is declared - a name not resolved, of a local declared after it in the block it
 * is in or one around it (the scope of a local is its whole block, C# spec 7.7.1)
 *
 * @author VISTALL
 */
public class CS0841 extends CompilerCheck<CSharpReferenceExpression> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpReferenceExpression element) {
        CSharpLocalVariable local = declaredLater(element);
        return local != null ? newBuilder(element, local.getName()) : null;
    }

    @RequiredReadAction
    static @Nullable CSharpLocalVariable declaredLater(CSharpReferenceExpression element) {
        String name = element.getReferenceName();
        if (name == null || element.getQualifier() != null || element.resolve() != null) {
            return null;
        }
        int offset = element.getTextOffset();
        for (PsiElement parent = element.getParent(); parent != null && !(parent instanceof CSharpSimpleLikeMethodAsElement); parent = parent.getParent()) {
            if (parent instanceof CSharpBlockStatementImpl block) {
                CSharpLocalVariable local = find(block, name, offset);
                if (local != null) {
                    return local;
                }
            }
        }
        return null;
    }

    /**
     * A local of the name declared in the block after the offset - a labeled statement holds the statements after it
     */
    @RequiredReadAction
    private static @Nullable CSharpLocalVariable find(PsiElement block, String name, int offset) {
        for (PsiElement child = block.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof CSharpLocalVariableDeclarationStatement declaration && child.getTextOffset() > offset) {
                for (CSharpLocalVariable variable : declaration.getVariables()) {
                    if (name.equals(variable.getName())) {
                        return variable;
                    }
                }
            }
            else if (child instanceof CSharpLabeledStatementImpl) {
                CSharpLocalVariable local = find(child, name, offset);
                if (local != null) {
                    return local;
                }
            }
        }
        return null;
    }
}
