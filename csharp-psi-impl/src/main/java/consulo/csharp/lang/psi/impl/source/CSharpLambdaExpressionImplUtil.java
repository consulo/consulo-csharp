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

package consulo.csharp.lang.psi.impl.source;


import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpUndefinedLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpLambdaExpressionImplUtil
{
	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef resolveTypeForParameter(CSharpLambdaExpressionImpl target, int parameterIndex)
	{
		CSharpLambdaResolveResult leftTypeRef = resolveLeftLambdaTypeRef(target);
		if(leftTypeRef == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(leftTypeRef == CSharpUndefinedLambdaResolveResult.INSTANCE)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		DotNetTypeRef[] leftTypeParameters = leftTypeRef.getParameterTypeRefs();
		DotNetTypeRef typeRef = ArrayUtil2.safeGet(leftTypeParameters, parameterIndex);
		return ObjectUtil.notNull(typeRef, DotNetTypeRef.ERROR_TYPE);
	}

	@Nullable
	@RequiredReadAction
	public static CSharpLambdaResolveResult resolveLeftLambdaTypeRef(PsiElement target)
	{
		if(target instanceof CSharpLambdaExpressionImpl)
		{
			DotNetTypeRef typeRefOfLambda = ((CSharpLambdaExpressionImpl) target).getInferenceSessionTypeRef();
			if(typeRefOfLambda != null)
			{
				DotNetTypeResolveResult typeResolveResult = typeRefOfLambda.resolve();
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
				}
				return null;
			}
		}

		return resolveLambdaTypeRefFromParent(target);
	}

	@Nullable
	@RequiredReadAction
	private static CSharpLambdaResolveResult resolveLambdaTypeRefFromParent(PsiElement target)
	{
		PsiElement parent = target.getParent();
		if(parent instanceof DotNetVariable)
		{
			DotNetVariable variable = (DotNetVariable) parent;
			if(variable.getInitializer() != target)
			{
				return null;
			}
			return resolveLeftLambdaTypeRefForVariable(variable);
		}
		else if(parent instanceof CSharpNamedFieldOrPropertySet)
		{
			CSharpNamedFieldOrPropertySet fieldOrPropertySet = (CSharpNamedFieldOrPropertySet) parent;
			if(fieldOrPropertySet.getValueExpression() != target)
			{
				return null;
			}

			CSharpReferenceExpression nameReferenceExpression = fieldOrPropertySet.getNameElement();
			PsiElement resolvedElement = nameReferenceExpression.resolve();
			if(resolvedElement instanceof DotNetVariable)
			{
				return resolveLeftLambdaTypeRefForVariable((DotNetVariable) resolvedElement);
			}
			return null;
		}
		else if(parent instanceof CSharpCallArgument)
		{
			CSharpCallArgumentListOwner argumentListOwner = PsiTreeUtil.getParentOfType(parent, CSharpCallArgumentListOwner.class, false);
			if(argumentListOwner == null)
			{
				return null;
			}

			ResolveResult validOrFirstMaybeResult = CSharpResolveUtil.findValidOrFirstMaybeResult(argumentListOwner.multiResolve(false));
			if(validOrFirstMaybeResult == null)
			{
				return CSharpUndefinedLambdaResolveResult.INSTANCE;
			}

			if(validOrFirstMaybeResult instanceof MethodResolveResult)
			{
				List<NCallArgument> arguments = ((MethodResolveResult) validOrFirstMaybeResult).getCalcResult().getArguments();
				for(NCallArgument argument : arguments)
				{
					if(argument.getCallArgument() == null)
					{
						continue;
					}
					CSharpCallArgument callArgument = argument.getCallArgument();
					if(callArgument.getArgumentExpression() == target)
					{
						DotNetTypeRef parameterTypeRef = argument.getParameterTypeRef();
						if(parameterTypeRef == null)
						{
							return null;
						}

						DotNetTypeResolveResult typeResolveResult = parameterTypeRef.resolve();
						if(typeResolveResult instanceof CSharpLambdaResolveResult)
						{
							return (CSharpLambdaResolveResult) typeResolveResult;
						}
						return null;
					}
				}
			}
		}
		else if(parent instanceof CSharpAssignmentExpressionImpl)
		{
			IElementType operatorElementType = ((CSharpAssignmentExpressionImpl) parent).getOperatorElement().getOperatorElementType();

			if(operatorElementType == CSharpTokens.PLUSEQ || operatorElementType == CSharpTokens.MINUSEQ || operatorElementType == CSharpTokens.EQ)
			{
				DotNetExpression expression = ((CSharpAssignmentExpressionImpl) parent).getParameterExpressions()[0];
				if(expression == target)
				{
					return null;
				}
				DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(true).resolve();
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
				}
			}
		}
		else if(parent instanceof CSharpTypeCastExpressionImpl)
		{
			DotNetTypeResolveResult typeResolveResult = ((CSharpTypeCastExpressionImpl) parent).toTypeRef(false).resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return (CSharpLambdaResolveResult) typeResolveResult;
			}
		}
		else if(parent instanceof CSharpReturnStatementImpl)
		{
			CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(parent, CSharpSimpleLikeMethodAsElement.class);
			if(methodAsElement == null)
			{
				return null;
			}
			DotNetTypeResolveResult typeResolveResult = methodAsElement.getReturnTypeRef().resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return (CSharpLambdaResolveResult) typeResolveResult;
			}
		}
		else if(parent instanceof CSharpParenthesesExpressionImpl)
		{
			return resolveLambdaTypeRefFromParent(parent);
		}
		else if(parent instanceof CSharpConditionalExpressionImpl)
		{
			DotNetExpression expression = ((CSharpConditionalExpressionImpl) parent).getTrueExpression();
			if(expression != null && expression != target)
			{
				DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(false).resolve();
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
				}
			}
			expression = ((CSharpConditionalExpressionImpl) parent).getFalseExpression();
			if(expression != null && expression != target)
			{
				DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(false).resolve();
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
				}
			}
		}
		else if(parent instanceof CSharpLambdaExpressionImpl)
		{
			if(((CSharpLambdaExpressionImpl) parent).getCodeBlock() != target)
			{
				return null;
			}

			DotNetTypeRef typeRef = ((CSharpLambdaExpressionImpl) parent).toTypeRef(true);

			DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				DotNetTypeRef returnTypeRef = ((CSharpLambdaResolveResult) typeResolveResult).getReturnTypeRef();

				DotNetTypeResolveResult returnTypeResult = returnTypeRef.resolve();
				if(returnTypeResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) returnTypeResult;
				}
			}
		}

		return null;
	}

	@Nullable
	@RequiredReadAction
	public static CSharpLambdaResolveResult resolveLeftLambdaTypeRefForVariable(DotNetVariable variable)
	{
		DotNetTypeRef leftTypeRef = variable.toTypeRef(false);
		DotNetTypeResolveResult typeResolveResult = leftTypeRef.resolve();
		return typeResolveResult instanceof CSharpLambdaResolveResult ? (CSharpLambdaResolveResult) typeResolveResult : null;
	}
}
