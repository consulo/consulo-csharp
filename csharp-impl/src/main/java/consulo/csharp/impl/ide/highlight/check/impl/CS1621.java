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
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpYieldStatementImpl;
import org.jspecify.annotations.Nullable;

/**
 * {@code () => { yield return 1; }} - the yield statement cannot be used inside an anonymous method or a lambda
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1621 extends CompilerCheck<CSharpYieldStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpYieldStatementImpl element) {
        if (!(PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class) instanceof CSharpAnonymousMethodExpression)) {
            return null;
        }
        return newBuilder(element.getFirstChild());
    }
}
