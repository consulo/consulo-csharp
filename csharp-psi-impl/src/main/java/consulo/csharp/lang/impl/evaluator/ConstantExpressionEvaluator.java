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

package consulo.csharp.lang.impl.evaluator;

import consulo.annotation.access.RequiredReadAction;
import consulo.component.ProcessCanceledException;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpAsExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpCheckedExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConditionalExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpDefaultExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpNameOfExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpSizeOfExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPrefixExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTypeCastExpressionImpl;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 28.08.14
 */
public class ConstantExpressionEvaluator extends CSharpElementVisitor {
    private final boolean myConstantOnly;

    private Object myValue;

    public ConstantExpressionEvaluator(@Nullable DotNetExpression expression) {
        this(expression, false);
    }

    /**
     * @param constantOnly only a constant expression of the C# spec (12.23) - {@code const} variables, not {@code readonly} ones
     */
    public ConstantExpressionEvaluator(@Nullable DotNetExpression expression, boolean constantOnly) {
        myConstantOnly = constantOnly;
        if (expression != null) {
            expression.accept(this);
        }
    }

    /**
     * The value of a constant bool expression - {@code false}, {@code !true}, {@code 1 == 1}, a {@code const bool} - else null
     */
    @Nullable
    public static Boolean evaluateBoolean(@Nullable DotNetExpression expression) {
        return new ConstantExpressionEvaluator(expression, true).getValueAs(Boolean.class);
    }

    /**
     * A constant expression of the C# spec (12.23) - literals, null, default, const variables and enum members, sizeof,
     * nameof, and the operators, casts and checked of them; its value need not be computable here
     */
    @RequiredReadAction
    public static boolean isConstantExpression(@Nullable DotNetExpression expression) {
        if (expression instanceof CSharpParenthesesExpressionImpl parentheses) {
            return isConstantExpression(parentheses.getInnerExpression());
        }
        if (expression instanceof CSharpCheckedExpressionImpl checkedExpression) {
            return isConstantExpression(checkedExpression.getInnerExpression());
        }
        if (expression instanceof CSharpTypeCastExpressionImpl cast) {
            return isConstantExpression(cast.getInnerExpression());
        }
        if (expression instanceof CSharpConstantExpressionImpl || expression instanceof CSharpDefaultExpressionImpl
            || expression instanceof CSharpSizeOfExpressionImpl || expression instanceof CSharpNameOfExpressionImpl) {
            return true;
        }
        if (expression instanceof CSharpPrefixExpressionImpl prefix) {
            IElementType operator = prefix.getOperatorElement().getOperatorElementType();
            return (operator == CSharpTokens.MINUS || operator == CSharpTokens.PLUS || operator == CSharpTokens.EXCL || operator == CSharpTokens.TILDE)
                && isConstantExpression(prefix.getExpression());
        }
        if (expression instanceof CSharpBinaryExpressionImpl binary) {
            return isConstantExpression(binary.getLeftExpression()) && isConstantExpression(binary.getRightExpression());
        }
        if (expression instanceof CSharpConditionalExpressionImpl conditional) {
            return isConstantExpression(conditional.getConditionExpression())
                && isConstantExpression(conditional.getTrueExpression())
                && isConstantExpression(conditional.getFalseExpression());
        }
        if (expression instanceof CSharpReferenceExpression reference) {
            return reference.resolve() instanceof DotNetVariable variable && variable.isConstant();
        }
        return false;
    }

    @Override
    public void visitConstantExpression(CSharpConstantExpressionImpl expression) {
        try {
            myValue = expression.getValue();
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (Exception e) {
            //
        }
    }

    @Override
    public void visitBinaryExpression(CSharpBinaryExpressionImpl expression) {
        DotNetExpression leftExpression = expression.getLeftExpression();
        DotNetExpression rightExpression = expression.getRightExpression();
        if (leftExpression == null || rightExpression == null) {
            return;
        }

        Object leftValue = new ConstantExpressionEvaluator(leftExpression, myConstantOnly).getValue();
        Object rightValue = new ConstantExpressionEvaluator(rightExpression, myConstantOnly).getValue();
        if (leftValue == null || rightValue == null) {
            return;
        }

        myValue = OperatorEvaluator.calcBinary(expression.getOperatorElement().getOperatorElementType(), leftValue, rightValue);
    }

    @Override
    public void visitParenthesesExpression(CSharpParenthesesExpressionImpl expression) {
        myValue = new ConstantExpressionEvaluator(expression.getInnerExpression(), myConstantOnly).getValue();
    }

    @Override
    public void visitPrefixExpression(CSharpPrefixExpressionImpl expression) {
        Object value = new ConstantExpressionEvaluator(expression.getExpression(), myConstantOnly).getValue();
        if (value != null) {
            myValue = OperatorEvaluator.calcPrefix(expression.getOperatorElement().getOperatorElementType(), value);
        }
    }

    @Override
    public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression) {
        myValue = castTo(new ConstantExpressionEvaluator(expression.getInnerExpression(), myConstantOnly).getValue(), expression);
    }

    @Override
    public void visitAsExpression(CSharpAsExpressionImpl expression) {
        myValue = castTo(new ConstantExpressionEvaluator(expression.getInnerExpression(), myConstantOnly).getValue(), expression);
    }

    @Override
    public void visitReferenceExpression(CSharpReferenceExpression expression) {
        PsiElement resolvedElement = expression.resolve();
        if (resolvedElement instanceof DotNetVariable) {
            if (((DotNetVariable) resolvedElement).isConstant() || !myConstantOnly && ((DotNetVariable) resolvedElement).hasModifier(CSharpModifier.READONLY)) {
                DotNetExpression initializer = ((DotNetVariable) resolvedElement).getInitializer();
                if (initializer == null) {
                    return;
                }
                myValue = new ConstantExpressionEvaluator(initializer, myConstantOnly).getValue();
            }
        }
    }

    private static Object castTo(Object value, DotNetExpression element) {
        if (value == null) {
            return null;
        }
        DotNetTypeRef typeRef = element.toTypeRef(false);
        PsiElement psiElement = typeRef.resolve().getElement();
        if (!(psiElement instanceof DotNetTypeDeclaration)) {
            return value;
        }

        String vmQName = ((DotNetTypeDeclaration) psiElement).getVmQName();
        if (DotNetTypes.System.Int32.equals(vmQName)) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        else if (DotNetTypes.System.Int16.equals(vmQName)) {
            if (value instanceof Number) {
                return ((Number) value).shortValue();
            }
        }
        else if (DotNetTypes.System.Int64.equals(vmQName)) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }
        else if (DotNetTypes.System.SByte.equals(vmQName)) {
            if (value instanceof Number) {
                return ((Number) value).byteValue();
            }
        }
        else if (DotNetTypes.System.Single.equals(vmQName)) {
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
        }
        else if (DotNetTypes.System.Double.equals(vmQName)) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }
        else if (DotNetTypes.System.String.equals(vmQName)) {
            return value.toString();
        }
        return value;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getValueAs(Class<T> clazz) {
        Object object = getValue();
        if (clazz.isInstance(object)) {
            return (T) object;
        }
        return null;
    }

    public Object getValue() {
        return myValue;
    }
}
