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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.RecursionManager;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.impl.psi.CSharpExpressionTreeUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.*;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.collection.ContainerUtil;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil {
    public static final int MAX_WEIGHT = Integer.MIN_VALUE;

    public static final Comparator<MethodResolveResult> ourComparator = (o1, o2) -> o2.getCalcResult().getWeight() - o1.getCalcResult().getWeight();

    @RequiredReadAction
    public static void sortAndProcess(List<MethodResolveResult> list, Processor<ResolveResult> processor, PsiElement place) {
        if (list.isEmpty()) {
            return;
        }

        ContainerUtil.sort(list, ourComparator);

        moveBetterToFront(list, place);

        for (MethodResolveResult methodResolveResult : list) {
            ProgressManager.checkCanceled();

            methodResolveResult.setAssignable(place);

            if (!processor.process(methodResolveResult)) {
                return;
            }
        }
    }

    /**
     * Among the valid candidates of the best weight, the better function member goes first (C# spec 12.6.4.3): its
     * parameter types are better conversion targets for some arguments and worse for none.
     */
    @RequiredReadAction
    private static void moveBetterToFront(List<MethodResolveResult> sortedList, PsiElement place) {
        if (sortedList.size() < 2 || !sortedList.get(0).isValidResult()) {
            return;
        }

        int bestWeight = sortedList.get(0).getCalcResult().getWeight();
        List<MethodResolveResult> tied = new ArrayList<>();
        for (MethodResolveResult result : sortedList) {
            if (!result.isValidResult() || result.getCalcResult().getWeight() != bestWeight) {
                break;
            }
            tied.add(result);
        }
        if (tied.size() < 2) {
            return;
        }

        GlobalSearchScope scope = place.getResolveScope();
        for (MethodResolveResult candidate : tied) {
            boolean betterThanAll = true;
            for (MethodResolveResult other : tied) {
                if (other != candidate && !isBetter(candidate, other, scope)) {
                    betterThanAll = false;
                    break;
                }
            }
            if (betterThanAll) {
                sortedList.remove(candidate);
                sortedList.add(0, candidate);
                return;
            }
        }
    }

    @RequiredReadAction
    private static boolean isBetter(MethodResolveResult first, MethodResolveResult second, GlobalSearchScope scope) {
        List<NCallArgument> firstArguments = first.getCalcResult().getArguments();
        List<NCallArgument> secondArguments = second.getCalcResult().getArguments();
        if (firstArguments.size() != secondArguments.size()) {
            return false;
        }

        int result = 0;
        boolean resultDowngradable = false;
        for (int i = 0; i < firstArguments.size(); i++) {
            DotNetTypeRef firstType = firstArguments.get(i).getParameterTypeRef();
            DotNetTypeRef secondType = secondArguments.get(i).getParameterTypeRef();
            if (firstType == null || secondType == null) {
                continue;
            }
            DotNetExpression expression = argumentExpression(firstArguments.get(i));
            int conversion = compareConversions(expression, firstType, secondType, scope);
            if (conversion == 0) {
                continue;
            }

            // Roslyn's legacy rule (bug 11961): what a lambda argument tells is dropped where another argument tells otherwise
            boolean downgradable = isDowngradableLambda(expression, firstType, secondType);
            if (result == 0) {
                result = conversion;
                resultDowngradable = downgradable;
            }
            else if (result != conversion) {
                if (resultDowngradable) {
                    result = conversion;
                    resultDowngradable = downgradable;
                }
                else if (!downgradable) {
                    return false;
                }
            }
            else {
                resultDowngradable &= downgradable;
            }
        }
        if (result != 0) {
            return result > 0;
        }

        // for the same conversions: a parameter by value is better than an in parameter for an argument passed by value
        boolean betterByValue = false;
        for (int i = 0; i < firstArguments.size(); i++) {
            boolean firstIn = isInParameter(firstArguments.get(i));
            boolean secondIn = isInParameter(secondArguments.get(i));
            if (firstIn && !secondIn) {
                return false;
            }
            betterByValue |= secondIn && !firstIn;
        }
        return betterByValue;
    }

    /**
     * Better conversion from expression (C# spec 12.6.4.5): the conversion to the type the argument exactly matches, else
     * to the better conversion target.
     *
     * @return 1 if the conversion to {@code first} is better, -1 if the one to {@code second}, 0 if neither
     */
    @RequiredReadAction
    private static int compareConversions(@Nullable DotNetExpression expression, DotNetTypeRef first, DotNetTypeRef second, GlobalSearchScope scope) {
        if (expression != null) {
            boolean firstExact = exactlyMatches(expression, first);
            boolean secondExact = exactlyMatches(expression, second);
            if (firstExact != secondExact) {
                return firstExact ? 1 : -1;
            }
        }
        if (isBetterConversionTarget(second, first, scope)) {
            return -1;
        }
        return isBetterConversionTarget(first, second, scope) ? 1 : 0;
    }

    /**
     * The expression has the type, or is a lambda whose returned expressions all exactly match the return type of the
     * delegate - {@code () => 0} exactly matches {@code Func<int>}, not {@code Func<short>}.
     */
    @RequiredReadAction
    private static boolean exactlyMatches(DotNetExpression expression, DotNetTypeRef typeRef) {
        Boolean result = RecursionManager.doPreventingRecursion(expression, false, () -> exactlyMatchesImpl(expression, typeRef));
        return result == Boolean.TRUE;
    }

    @RequiredReadAction
    private static boolean exactlyMatchesImpl(DotNetExpression expression, DotNetTypeRef typeRef) {
        while (expression instanceof CSharpParenthesesExpressionImpl parentheses) {
            expression = parentheses.getInnerExpression();
        }
        if (expression == null) {
            return false;
        }

        if (expression instanceof CSharpLambdaExpressionImpl lambda) {
            if (!(CSharpExpressionTreeUtil.unwrap(typeRef).resolve() instanceof CSharpLambdaResolveResult delegate)) {
                return false;
            }
            DotNetTypeRef returnType = delegate.getReturnTypeRef();
            if (returnType == DotNetTypeRef.AUTO_TYPE || DotNetTypeRefUtil.isVmQNameEqual(returnType, DotNetTypes.System.Void)) {
                return false;
            }
            List<DotNetExpression> returned = lambda.getReturnExpressions();
            if (returned.isEmpty()) {
                return false;
            }
            for (DotNetExpression returnedExpression : returned) {
                if (returnedExpression == null || !exactlyMatches(returnedExpression, returnType)) {
                    return false;
                }
            }
            return true;
        }

        if (expression instanceof CSharpDelegateExpressionImpl) {
            return false;
        }

        DotNetTypeRef expressionType = expression.toTypeRef(false);
        if (expressionType == DotNetTypeRef.ERROR_TYPE || expressionType instanceof CSharpNullTypeRef || expressionType instanceof CSharpElementGroupTypeRef) {
            return false;
        }
        return CSharpTypeUtil.isTypeEqual(expressionType, typeRef);
    }

    /**
     * A lambda converted to delegates of different parameters, or of the same return - its betterness gives way to the
     * other arguments (Roslyn's {@code CanDowngradeConversionFromLambdaToNeither}).
     */
    @RequiredReadAction
    private static boolean isDowngradableLambda(@Nullable DotNetExpression expression, DotNetTypeRef first, DotNetTypeRef second) {
        while (expression instanceof CSharpParenthesesExpressionImpl parentheses) {
            expression = parentheses.getInnerExpression();
        }
        if (!(expression instanceof CSharpLambdaExpressionImpl) && !(expression instanceof CSharpDelegateExpressionImpl)) {
            return false;
        }
        if (!(CSharpExpressionTreeUtil.unwrap(first).resolve() instanceof CSharpLambdaResolveResult firstDelegate)
            || !(CSharpExpressionTreeUtil.unwrap(second).resolve() instanceof CSharpLambdaResolveResult secondDelegate)) {
            return false;
        }

        DotNetTypeRef[] firstParameters = firstDelegate.getParameterTypeRefs();
        DotNetTypeRef[] secondParameters = secondDelegate.getParameterTypeRefs();
        if (firstParameters.length != secondParameters.length) {
            return true;
        }
        for (int i = 0; i < firstParameters.length; i++) {
            if (!CSharpTypeUtil.isTypeEqual(firstParameters[i], secondParameters[i])) {
                return true;
            }
        }

        DotNetTypeRef firstReturn = firstDelegate.getReturnTypeRef();
        DotNetTypeRef secondReturn = secondDelegate.getReturnTypeRef();
        boolean firstVoid = DotNetTypeRefUtil.isVmQNameEqual(firstReturn, DotNetTypes.System.Void);
        boolean secondVoid = DotNetTypeRefUtil.isVmQNameEqual(secondReturn, DotNetTypes.System.Void);
        if (firstVoid || secondVoid) {
            return firstVoid && secondVoid;
        }
        return CSharpTypeUtil.isTypeEqual(firstReturn, secondReturn);
    }

    @Nullable
    private static DotNetExpression argumentExpression(NCallArgument argument) {
        CSharpCallArgument callArgument = argument.getCallArgument();
        return callArgument == null ? null : callArgument.getArgumentExpression();
    }

    @RequiredReadAction
    private static boolean isInParameter(NCallArgument argument) {
        DotNetParameter parameter = argument.getParameter();
        return parameter != null && parameter.hasModifier(CSharpModifier.IN);
    }

    /**
     * T1 is better than T2 if T1 converts to T2 implicitly and not the other way; delegates compare by their return types
     * (a lambda converts to both).
     */
    @RequiredReadAction
    private static boolean isBetterConversionTarget(DotNetTypeRef first, DotNetTypeRef second, GlobalSearchScope scope) {
        // dynamic is object here - F(C) is better than F(dynamic) for F(null)
        first = dynamicAsObject(first, scope);
        second = dynamicAsObject(second, scope);
        if (CSharpTypeUtil.isTypeEqual(first, second)) {
            return false;
        }

        boolean firstToSecond = CSharpTypeUtil.isInheritable(second, first, CSharpCastType.IMPLICIT, scope).isSuccess();
        boolean secondToFirst = CSharpTypeUtil.isInheritable(first, second, CSharpCastType.IMPLICIT, scope).isSuccess();
        if (firstToSecond != secondToFirst) {
            return firstToSecond;
        }

        // delegates neither converting to the other compare by their return types
        if (first.resolve() instanceof CSharpLambdaResolveResult firstLambda && second.resolve() instanceof CSharpLambdaResolveResult secondLambda) {
            DotNetTypeRef firstReturn = firstLambda.getReturnTypeRef();
            DotNetTypeRef secondReturn = secondLambda.getReturnTypeRef();
            boolean firstVoid = DotNetTypeRefUtil.isVmQNameEqual(firstReturn, DotNetTypes.System.Void);
            boolean secondVoid = DotNetTypeRefUtil.isVmQNameEqual(secondReturn, DotNetTypes.System.Void);
            // a delegate returning a value is better than a void one
            if (firstVoid || secondVoid) {
                return !firstVoid;
            }
            return isBetterConversionTarget(firstReturn, secondReturn, scope);
        }
        return false;
    }

    private static DotNetTypeRef dynamicAsObject(DotNetTypeRef typeRef, GlobalSearchScope scope) {
        return typeRef instanceof CSharpDynamicTypeRef ? new CSharpTypeRefByQName(typeRef.getProject(), scope, DotNetTypes.System.Object) : typeRef;
    }
}
