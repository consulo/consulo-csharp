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
import consulo.csharp.lang.impl.psi.source.CSharpAwaitExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLockStatementImpl;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * Cannot await in the body of a lock statement - up to the method or lambda; the locked expression may await
 *
 * @author VISTALL
 */
public class CS1996 extends CompilerCheck<CSharpAwaitExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAwaitExpressionImpl element) {
        PsiElement child = element;
        for (PsiElement parent = element.getParent(); parent != null && !(parent instanceof CSharpSimpleLikeMethodAsElement); child = parent, parent = parent.getParent()) {
            if (parent instanceof CSharpLockStatementImpl && child instanceof DotNetStatement) {
                return newBuilder(element.getFirstChild());
            }
        }
        return null;
    }
}
