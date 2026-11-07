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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpParenthesesExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTypeCastExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.util.PsiTreeUtil;
import org.jspecify.annotations.Nullable;

/**
 * Expression trees: a lambda converts to {@code System.Linq.Expressions.Expression<TDelegate>} the way it converts to
 * {@code TDelegate} - {@code Expression<Func<T, object>> e = o => o.Name;} types {@code o} by the {@code Func}.
 *
 * @author VISTALL
 */
public final class CSharpExpressionTreeUtil {
    public static final String EXPRESSION_VM_QNAME = "System.Linq.Expressions.Expression`1";

    private CSharpExpressionTreeUtil() {
    }

    /**
     * The lambda converted to an expression tree which the element is a part of - a lambda inside it is a part of the tree too
     */
    @Nullable
    @RequiredReadAction
    public static CSharpLambdaExpressionImpl findExpressionTree(PsiElement element) {
        CSharpLambdaExpressionImpl tree = null;
        for (PsiElement parent = element; parent != null; parent = parent.getParent()) {
            if (parent instanceof DotNetLikeMethodDeclaration) {
                break;
            }
            if (parent instanceof CSharpLambdaExpressionImpl lambda && isExpressionTree(lambda)) {
                tree = lambda;
            }
        }
        return tree;
    }

    /**
     * @return true if the lambda converts to {@code Expression<TDelegate>} - by the type it is converted to
     */
    @RequiredReadAction
    public static boolean isExpressionTree(CSharpLambdaExpressionImpl lambda) {
        return LanguageCachedValueUtil.getCachedValue(lambda, () -> {
            DotNetTypeRef targetTypeRef = targetTypeRef(lambda);
            boolean tree = targetTypeRef != null && isExpressionType(targetTypeRef);
            return CachedValueProvider.Result.create(tree, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }

    @RequiredReadAction
    private static boolean isExpressionType(DotNetTypeRef typeRef) {
        return typeRef.resolve().getElement() instanceof DotNetTypeDeclaration type && EXPRESSION_VM_QNAME.equals(type.getVmQName());
    }

    /**
     * The type a lambda or an anonymous method converts to, from its place - before {@link #unwrap}
     */
    @Nullable
    @RequiredReadAction
    private static DotNetTypeRef targetTypeRef(PsiElement target) {
        PsiElement parent = target.getParent();
        if (parent instanceof CSharpParenthesesExpressionImpl) {
            return targetTypeRef(parent);
        }
        if (parent instanceof DotNetVariable variable && variable.getInitializer() == target) {
            return variable.toTypeRef(false);
        }
        if (parent instanceof CSharpNamedFieldOrPropertySet set && set.getValueExpression() == target) {
            return set.getNameElement().resolve() instanceof DotNetVariable variable ? variable.toTypeRef(false) : null;
        }
        if (parent instanceof CSharpAssignmentExpressionImpl assignment
            && assignment.getOperatorElement().getOperatorElementType() == CSharpTokens.EQ) {
            DotNetExpression[] expressions = assignment.getParameterExpressions();
            return expressions.length == 2 && expressions[1] == target ? expressions[0].toTypeRef(true) : null;
        }
        if (parent instanceof CSharpTypeCastExpressionImpl cast) {
            return cast.toTypeRef(false);
        }
        if (parent instanceof CSharpReturnStatementImpl) {
            CSharpSimpleLikeMethodAsElement method = PsiTreeUtil.getParentOfType(parent, CSharpSimpleLikeMethodAsElement.class);
            return method == null ? null : method.getReturnTypeRef();
        }
        if (parent instanceof CSharpCallArgument) {
            CSharpCallArgumentListOwner call = PsiTreeUtil.getParentOfType(parent, CSharpCallArgumentListOwner.class);
            if (call == null) {
                return null;
            }
            // the valid candidate - else the first one
            DotNetTypeRef first = null;
            for (ResolveResult result : call.multiResolve(true)) {
                if (!(result instanceof MethodResolveResult methodResult)) {
                    continue;
                }
                for (NCallArgument argument : methodResult.getCalcResult().getArguments()) {
                    CSharpCallArgument callArgument = argument.getCallArgument();
                    if (callArgument != null && callArgument.getArgumentExpression() == target && argument.getParameterTypeRef() != null) {
                        if (result.isValidResult()) {
                            return argument.getParameterTypeRef();
                        }
                        if (first == null) {
                            first = argument.getParameterTypeRef();
                        }
                    }
                }
            }
            return first;
        }
        return null;
    }

    /**
     * @return {@code TDelegate} for {@code Expression<TDelegate>}, the type itself otherwise
     */
    @RequiredReadAction
    public static DotNetTypeRef unwrap(DotNetTypeRef typeRef) {
        DotNetTypeResolveResult result = typeRef.resolve();
        if (result.getElement() instanceof DotNetTypeDeclaration type && EXPRESSION_VM_QNAME.equals(type.getVmQName())) {
            DotNetGenericParameter[] genericParameters = type.getGenericParameters();
            if (genericParameters.length == 1) {
                DotNetTypeRef delegateTypeRef = result.getGenericExtractor().extract(genericParameters[0]);
                if (delegateTypeRef != null) {
                    return delegateTypeRef;
                }
            }
        }
        return typeRef;
    }
}
