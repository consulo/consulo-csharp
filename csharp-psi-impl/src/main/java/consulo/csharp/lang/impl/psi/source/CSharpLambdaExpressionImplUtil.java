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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpExpressionTreeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpUndefinedLambdaResolveResult;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.ObjectUtil;
import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpLambdaExpressionImplUtil {
    @RequiredReadAction
    public static DotNetTypeRef resolveTypeForParameter(CSharpLambdaExpressionImpl target, int parameterIndex) {
        CSharpLambdaResolveResult leftTypeRef = resolveLeftLambdaTypeRef(target);
        if (leftTypeRef == null) {
            return DotNetTypeRef.ERROR_TYPE;
        }

        if (leftTypeRef == CSharpUndefinedLambdaResolveResult.INSTANCE) {
            return DotNetTypeRef.UNKNOWN_TYPE;
        }
        DotNetTypeRef[] leftTypeParameters = leftTypeRef.getParameterTypeRefs();
        DotNetTypeRef typeRef = ArrayUtil2.safeGet(leftTypeParameters, parameterIndex);
        return ObjectUtil.notNull(typeRef, DotNetTypeRef.ERROR_TYPE);
    }

    @Nullable
    @RequiredReadAction
    public static CSharpLambdaResolveResult resolveLeftLambdaTypeRef(PsiElement target) {
        if (target instanceof CSharpLambdaExpressionImpl) {
            DotNetTypeRef typeRefOfLambda = ((CSharpLambdaExpressionImpl) target).getInferenceSessionTypeRef();
            if (typeRefOfLambda != null) {
                DotNetTypeResolveResult typeResolveResult = typeRefOfLambda.resolve();
                if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                    return (CSharpLambdaResolveResult) typeResolveResult;
                }
                return null;
            }
        }

        return resolveLambdaTypeRefFromParent(target);
    }

    @Nullable
    @RequiredReadAction
    private static CSharpLambdaResolveResult resolveLambdaTypeRefFromParent(PsiElement target) {
        PsiElement parent = target.getParent();
        if (parent instanceof DotNetVariable) {
            DotNetVariable variable = (DotNetVariable) parent;
            if (variable.getInitializer() != target) {
                return null;
            }
            return resolveLeftLambdaTypeRefForVariable(variable);
        }
        else if (parent instanceof CSharpNamedFieldOrPropertySet) {
            CSharpNamedFieldOrPropertySet fieldOrPropertySet = (CSharpNamedFieldOrPropertySet) parent;
            if (fieldOrPropertySet.getValueExpression() != target) {
                return null;
            }

            CSharpReferenceExpression nameReferenceExpression = fieldOrPropertySet.getNameElement();
            PsiElement resolvedElement = nameReferenceExpression.resolve();
            if (resolvedElement instanceof DotNetVariable) {
                return resolveLeftLambdaTypeRefForVariable((DotNetVariable) resolvedElement);
            }
            return null;
        }
        else if (parent instanceof CSharpCallArgument) {
            CSharpCallArgumentListOwner argumentListOwner = PsiTreeUtil.getParentOfType(parent, CSharpCallArgumentListOwner.class, false);
            if (argumentListOwner == null) {
                return null;
            }

            // every candidate, the valid ones are taken first below
            ResolveResult[] resolveResults = argumentListOwner.multiResolve(true);
            if (resolveResults.length == 0) {
                return CSharpUndefinedLambdaResolveResult.INSTANCE;
            }

            // the valid candidates first; in code which does not compile (yet) the best candidate whose parameter at the
            // position of the lambda is a delegate it fits - x in FirstOrDefault(x => x.) is typed while it is written -
            // and at last any delegate there, (x, y) => x for Action<T, T, int> still types x
            for (int pass = 0; pass < 3; pass++) {
                for (ResolveResult resolveResult : resolveResults) {
                    if (pass == 0 && !resolveResult.isValidResult() || !(resolveResult instanceof MethodResolveResult methodResolveResult)) {
                        continue;
                    }

                    CSharpLambdaResolveResult delegate = delegateForArgument(methodResolveResult, target, pass < 2);
                    if (delegate != null) {
                        return delegate;
                    }
                }
            }
            return null;
        }
        else if (parent instanceof CSharpAssignmentExpressionImpl) {
            IElementType operatorElementType = ((CSharpAssignmentExpressionImpl) parent).getOperatorElement().getOperatorElementType();

            if (operatorElementType == CSharpTokens.PLUSEQ || operatorElementType == CSharpTokens.MINUSEQ || operatorElementType == CSharpTokens.EQ) {
                DotNetExpression expression = ((CSharpAssignmentExpressionImpl) parent).getParameterExpressions()[0];
                if (expression == target) {
                    return null;
                }
                DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(true).resolve();
                if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                    return (CSharpLambdaResolveResult) typeResolveResult;
                }
            }
        }
        else if (parent instanceof CSharpTypeCastExpressionImpl) {
            DotNetTypeResolveResult typeResolveResult = ((CSharpTypeCastExpressionImpl) parent).toTypeRef(false).resolve();
            if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                return (CSharpLambdaResolveResult) typeResolveResult;
            }
        }
        else if (parent instanceof CSharpReturnStatementImpl) {
            CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parent, CSharpSimpleLikeMethodAsElement.class);
            if (methodAsElement == null) {
                return null;
            }
            DotNetTypeResolveResult typeResolveResult = methodAsElement.getReturnTypeRef().resolve();
            if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                return (CSharpLambdaResolveResult) typeResolveResult;
            }
        }
        else if (parent instanceof CSharpTupleElementImpl tupleElement) {
            if (parent.getParent() instanceof CSharpTupleExpressionImpl && parent.getParent().getParent() instanceof CSharpReturnStatementImpl returnStatement) {
                int position = tupleElement.getPosition();
                if (position == -1) {
                    return null;
                }

                CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(returnStatement, CSharpSimpleLikeMethodAsElement.class);
                if (methodAsElement == null) {
                    return null;
                }

                DotNetTypeResolveResult typeResolveResult = methodAsElement.getReturnTypeRef().resolve();

                if (typeResolveResult.getElement() instanceof DotNetTypeDeclaration typeDeclaration) {
                    DotNetGenericParameter parameter = ArrayUtil2.safeGet(typeDeclaration.getGenericParameters(), position);

                    if (parameter == null) {
                        return null;
                    }

                    DotNetTypeRef targetType = typeResolveResult.getGenericExtractor().extract(parameter);

                    if (targetType != null) {
                        DotNetTypeResolveResult targetResolveType = targetType.resolve();
                        if (targetResolveType instanceof CSharpLambdaResolveResult) {
                            return (CSharpLambdaResolveResult) targetResolveType;
                        }
                    }
                }
            }
        }
        else if (parent instanceof CSharpParenthesesExpressionImpl) {
            return resolveLambdaTypeRefFromParent(parent);
        }
        else if (parent instanceof CSharpConditionalExpressionImpl) {
            DotNetExpression expression = ((CSharpConditionalExpressionImpl) parent).getTrueExpression();
            if (expression != null && expression != target) {
                DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(false).resolve();
                if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                    return (CSharpLambdaResolveResult) typeResolveResult;
                }
            }
            expression = ((CSharpConditionalExpressionImpl) parent).getFalseExpression();
            if (expression != null && expression != target) {
                DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(false).resolve();
                if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                    return (CSharpLambdaResolveResult) typeResolveResult;
                }
            }
        }
        else if (parent instanceof CSharpLambdaExpressionImpl) {
            // the code block is a proxy - its element is the body expression
            if (((CSharpLambdaExpressionImpl) parent).getCodeBlock().getElement() != target) {
                return null;
            }

            DotNetTypeRef typeRef = ((CSharpLambdaExpressionImpl) parent).toTypeRef(true);

            DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
            if (typeResolveResult instanceof CSharpLambdaResolveResult) {
                DotNetTypeRef returnTypeRef = ((CSharpLambdaResolveResult) typeResolveResult).getReturnTypeRef();

                DotNetTypeResolveResult returnTypeResult = returnTypeRef.resolve();
                if (returnTypeResult instanceof CSharpLambdaResolveResult) {
                    return (CSharpLambdaResolveResult) returnTypeResult;
                }
            }
        }

        return null;
    }

    /**
     * The delegate type (or the delegate of the expression tree type) of the parameter the {@code target} argument goes
     * to in the candidate - with {@code checkArity}, only if a lambda {@code target} fits it by its number of parameters.
     */
    @Nullable
    @RequiredReadAction
    private static CSharpLambdaResolveResult delegateForArgument(MethodResolveResult methodResolveResult, PsiElement target, boolean checkArity) {
        for (NCallArgument argument : methodResolveResult.getCalcResult().getArguments()) {
            CSharpCallArgument callArgument = argument.getCallArgument();
            if (callArgument == null || callArgument.getArgumentExpression() != target) {
                continue;
            }

            DotNetTypeRef parameterTypeRef = argument.getParameterTypeRef();
            if (parameterTypeRef == null || !(CSharpExpressionTreeUtil.unwrap(parameterTypeRef).resolve() instanceof CSharpLambdaResolveResult delegate)) {
                return null;
            }
            if (checkArity && target instanceof CSharpLambdaExpressionImpl lambda && delegate.getParameterTypeRefs().length != lambda.getParameters().length) {
                return null;
            }
            return delegate;
        }
        return null;
    }

    @Nullable
    @RequiredReadAction
    public static CSharpLambdaResolveResult resolveLeftLambdaTypeRefForVariable(DotNetVariable variable) {
        DotNetTypeRef leftTypeRef = CSharpExpressionTreeUtil.unwrap(variable.toTypeRef(false));
        DotNetTypeResolveResult typeResolveResult = leftTypeRef.resolve();
        return typeResolveResult instanceof CSharpLambdaResolveResult ? (CSharpLambdaResolveResult) typeResolveResult : null;
    }
}
