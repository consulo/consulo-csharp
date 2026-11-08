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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpForStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * The variable is assigned but its value is never used - as Roslyn: a local of a declaration or a for initializer, never
 * read (nor passed as out or ref, nor in nameof), all its assignments - the initializer included - constants
 *
 * @author VISTALL
 * @since 20.05.14
 */
public class CS0219 extends CompilerCheck<CSharpLocalVariable> {
    @RequiredReadAction
    @Nullable
    @Override
    public HighlightInfoFactory checkImpl(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpLocalVariable element) {
        PsiElement parent = element.getParent();
        if (!(parent instanceof CSharpLocalVariableDeclarationStatement || parent instanceof CSharpForStatementImpl) || element.getNameIdentifier() == null
            // using var d = ...
            || PsiUtilCore.getElementType(PsiTreeUtil.getDeepestFirst(parent)) == CSharpTokens.USING_KEYWORD) {
            return null;
        }

        DotNetExpression initializer = element.getInitializer();
        if (initializer != null && !ConstantExpressionEvaluator.isConstantExpression(initializer)) {
            return null;
        }
        boolean assigned = initializer != null;
        boolean referenced = false;
        for (PsiReference reference : ReferencesSearch.search(element)) {
            referenced = true;
            DotNetExpression value = assignedValue(reference.getElement());
            if (value == null || !ConstantExpressionEvaluator.isConstantExpression(value)) {
                return null;
            }
            assigned = true;
        }
        if (!assigned) {
            // declared but never used - CS0168
            return null;
        }

        CompilerCheckBuilder builder = newBuilder(element.getNameIdentifier(), formatElement(element));
        if (!referenced) {
            builder.withQuickFix(new CS0168.DeleteLocalVariable(element));
        }
        return builder;
    }

    /**
     * The value of x = value with the reference as its target - null for any other use
     */
    @RequiredReadAction
    private static @Nullable DotNetExpression assignedValue(PsiElement reference) {
        PsiElement target = reference;
        while (target.getParent() instanceof CSharpParenthesesExpressionImpl parentheses) {
            target = parentheses;
        }
        if (target.getParent() instanceof CSharpAssignmentExpressionImpl assignment
            && assignment.getLeftExpression() == target
            && assignment.getOperatorElement().getOperatorElementType() == CSharpTokens.EQ) {
            return assignment.getRightExpression();
        }
        return null;
    }
}
