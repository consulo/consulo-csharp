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
import consulo.csharp.lang.impl.psi.CSharpExpressionTreeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionWithOperatorImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPostfixExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPrefixExpressionImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import org.jspecify.annotations.Nullable;

/**
 * An assignment - with {@code ++} and {@code --} - in an expression tree
 *
 * @author VISTALL
 */
public class CS0832 extends CompilerCheck<CSharpExpressionWithOperatorImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpExpressionWithOperatorImpl element) {
        if (!(element instanceof CSharpAssignmentExpressionImpl
                || (element instanceof CSharpPrefixExpressionImpl || element instanceof CSharpPostfixExpressionImpl)
                && (element.getOperatorElement().getOperatorElementType() == CSharpTokens.PLUSPLUS
                || element.getOperatorElement().getOperatorElementType() == CSharpTokens.MINUSMINUS)) || CSharpExpressionTreeUtil.findExpressionTree(element) == null) {
            return null;
        }
        return newBuilder(element);
    }
}
