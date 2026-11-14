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
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import org.jspecify.annotations.Nullable;

/**
 * {@code x == x} - comparison made to the same variable
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS1718 extends CompilerCheck<CSharpBinaryExpressionImpl> {
    private static final TokenSet COMPARISONS = TokenSet.create(CSharpTokens.EQEQ, CSharpTokens.NTEQ, CSharpTokens.LT, CSharpTokens.GT, CSharpTokens.LTEQ, CSharpTokens.GTEQ);

    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpBinaryExpressionImpl element) {
        IElementType operator = element.getOperatorElement().getOperatorElementType();
        if (!COMPARISONS.contains(operator) || !SameVariableUtil.isSameVariable(element.getLeftExpression(), element.getRightExpression())) {
            return null;
        }
        return newBuilder(element);
    }
}
