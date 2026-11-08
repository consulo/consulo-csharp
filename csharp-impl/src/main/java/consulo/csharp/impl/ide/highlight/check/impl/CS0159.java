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
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.controlFlow.CSharpSwitchSection;
import consulo.csharp.lang.impl.psi.source.CSharpCaseOrDefaultStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpGotoStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchLabelStatement;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchStatementImpl;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * No such label within the scope of the goto statement - a goto to a label not visible from it, a goto case with no
 * case of its value, a goto default with no default label in the switch
 *
 * @author VISTALL
 */
public class CS0159 extends CompilerCheck<CSharpGotoStatementImpl> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpGotoStatementImpl element) {
        DotNetExpression expression = element.getExpression();
        if (!element.isCaseOrDefault()) {
            if (expression instanceof CSharpReferenceExpression reference && reference.getReferenceElement() != null && reference.resolve() == null) {
                return newBuilder(reference, reference.getReferenceName());
            }
            return null;
        }

        // goto case outside a switch - CS0153
        CSharpSwitchStatementImpl switchStatement = PsiTreeUtil.getParentOfType(element, CSharpSwitchStatementImpl.class, true, CSharpSimpleLikeMethodAsElement.class);
        if (switchStatement == null) {
            return null;
        }
        PsiElement keyword = element.getFirstChild().getNextSibling();
        while (keyword != null && PsiUtilCore.getElementType(keyword) != CSharpTokens.CASE_KEYWORD && PsiUtilCore.getElementType(keyword) != CSharpTokens.DEFAULT_KEYWORD) {
            keyword = keyword.getNextSibling();
        }
        if (keyword == null) {
            return null;
        }

        boolean isDefault = PsiUtilCore.getElementType(keyword) == CSharpTokens.DEFAULT_KEYWORD;
        Object value = isDefault ? null : new ConstantExpressionEvaluator(expression, true).getValue();
        if (!isDefault && value == null) {
            return null;
        }
        for (CSharpSwitchSection section : CSharpSwitchSection.of(switchStatement)) {
            for (CSharpSwitchLabelStatement label : section.labels()) {
                if (!(label instanceof CSharpCaseOrDefaultStatementImpl caseLabel)) {
                    continue;
                }
                DotNetExpression labelExpression = caseLabel.getExpression();
                if (isDefault ? labelExpression == null : labelExpression != null && CS0152.sameValue(value, new ConstantExpressionEvaluator(labelExpression, true).getValue())) {
                    return null;
                }
            }
        }
        return newBuilder(element.getFirstChild(), isDefault ? "default:" : "case " + CS0152.present(value) + ":");
    }
}
