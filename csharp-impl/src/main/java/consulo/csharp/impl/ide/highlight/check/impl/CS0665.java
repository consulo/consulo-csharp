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
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConditionalExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpDoWhileStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpIfStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpWhileStatementImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

/**
 * {@code if (b = true)} - assignment in a conditional expression is always constant
 *
 * @author VISTALL
 * @since 2026-11-14
 */
public class CS0665 extends CompilerCheck<CSharpAssignmentExpressionImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpAssignmentExpressionImpl element) {
        if (element.getOperatorElement().getOperatorElementType() != CSharpTokens.EQ) {
            return null;
        }
        // if (b = true) - the condition of a statement or of ?: is the assignment of a bool constant
        PsiElement parent = element.getParent();
        DotNetExpression condition = parent instanceof CSharpIfStatementImpl s ? s.getConditionExpression()
            : parent instanceof CSharpWhileStatementImpl s ? s.getConditionExpression()
            : parent instanceof CSharpDoWhileStatementImpl s ? s.getConditionExpression()
            : parent instanceof CSharpConditionalExpressionImpl c ? c.getConditionExpression() : null;
        if (condition != element || !(element.getRightExpression() instanceof CSharpConstantExpressionImpl constant)) {
            return null;
        }
        IElementType literal = constant.getLiteralType();
        if (literal != CSharpTokens.TRUE_KEYWORD && literal != CSharpTokens.FALSE_KEYWORD) {
            return null;
        }
        return newBuilder(element);
    }
}
