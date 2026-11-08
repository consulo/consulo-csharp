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
import consulo.csharp.lang.impl.psi.source.CSharpCatchStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpFinallyStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpThrowStatementImpl;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * A throw statement with no arguments is not allowed outside of a catch clause - up to the method or lambda; in a
 * finally block in a catch - CS0724
 *
 * @author VISTALL
 */
public class CS0156 extends CompilerCheck<CSharpThrowStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpThrowStatementImpl element) {
        return rethrowPlace(element) == null ? newBuilder(element.getFirstChild()) : null;
    }

    /**
     * throw; - the catch it rethrows from, the finally block in it (CS0724), null for none; any - for a throw with a value
     */
    @RequiredReadAction
    static @Nullable PsiElement rethrowPlace(CSharpThrowStatementImpl statement) {
        if (statement.getExpression() != null) {
            return statement;
        }
        PsiElement finallyBlock = null;
        for (PsiElement parent = statement.getParent(); parent != null && !(parent instanceof CSharpSimpleLikeMethodAsElement); parent = parent.getParent()) {
            if (parent instanceof CSharpFinallyStatementImpl && finallyBlock == null) {
                finallyBlock = parent;
            }
            else if (parent instanceof CSharpCatchStatementImpl) {
                return finallyBlock != null ? finallyBlock : parent;
            }
        }
        return null;
    }
}
