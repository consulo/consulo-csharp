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
import consulo.csharp.lang.impl.psi.source.CSharpSwitchLabelStatement;
import consulo.csharp.lang.impl.psi.source.CSharpSwitchStatementImpl;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The switch statement contains multiple cases with the label value - a constant label, or default, after one of the
 * same value; at the case of the second one
 *
 * @author VISTALL
 */
public class CS0152 extends CompilerCheck<CSharpSwitchStatementImpl> {
    @RequiredReadAction
    @Override
    public List<? extends HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, CSharpSwitchStatementImpl element) {
        List<HighlightInfoFactory> results = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        boolean hasDefault = false;
        for (CSharpSwitchSection section : CSharpSwitchSection.of(element)) {
            for (CSharpSwitchLabelStatement label : section.labels()) {
                if (!(label instanceof CSharpCaseOrDefaultStatementImpl caseLabel)) {
                    continue;
                }
                DotNetExpression expression = caseLabel.getExpression();
                if (expression == null) {
                    if (hasDefault) {
                        results.add(newBuilder(label.getFirstChild(), "default"));
                    }
                    hasDefault = true;
                    continue;
                }
                Object value = valueOf(expression);
                if (value == null) {
                    continue;
                }
                if (values.stream().anyMatch(it -> sameValue(it, value))) {
                    results.add(newBuilder(label.getFirstChild(), present(value)));
                }
                else {
                    values.add(value);
                }
            }
        }
        return results;
    }

    /**
     * The constant - an enum member by its number
     */
    @RequiredReadAction
    private static @Nullable Object valueOf(DotNetExpression expression) {
        if (expression instanceof CSharpReferenceExpression reference && reference.resolve() instanceof CSharpEnumConstantDeclaration constant) {
            return enumValue(constant);
        }
        return new ConstantExpressionEvaluator(expression, true).getValue();
    }

    /**
     * Its initializer, or the one before it plus one, from zero
     */
    @RequiredReadAction
    private static @Nullable Object enumValue(CSharpEnumConstantDeclaration constant) {
        if (constant.getInitializer() != null) {
            Object value = new ConstantExpressionEvaluator(constant.getInitializer(), true).getValue();
            return value instanceof Number number ? number.longValue() : null;
        }
        if (!(constant.getParent() instanceof DotNetTypeDeclaration type)) {
            return null;
        }
        CSharpEnumConstantDeclaration previous = null;
        for (PsiElement member : type.getMembers()) {
            if (member == constant) {
                break;
            }
            if (member instanceof CSharpEnumConstantDeclaration declaration) {
                previous = declaration;
            }
        }
        if (previous == null) {
            return 0L;
        }
        Object value = enumValue(previous);
        return value instanceof Long number ? number + 1 : null;
    }

    static boolean sameValue(@Nullable Object first, @Nullable Object second) {
        if (first == null || second == null) {
            return false;
        }
        if (first instanceof Number firstNumber && second instanceof Number secondNumber
            && !(first instanceof Float || first instanceof Double || second instanceof Float || second instanceof Double)) {
            return firstNumber.longValue() == secondNumber.longValue();
        }
        return first.equals(second);
    }

    /**
     * As C# writes it - "a", 'c'
     */
    static String present(@Nullable Object value) {
        if (value instanceof String string) {
            return "\"" + string + "\"";
        }
        if (value instanceof Character character) {
            return "'" + character + "'";
        }
        return String.valueOf(value);
    }
}
