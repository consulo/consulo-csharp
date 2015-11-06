/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;


import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtil;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpLambdaExpressionImplUtil
{
	public static final Key<DotNetTypeRef> TYPE_REF_OF_LAMBDA = Key.create("type.ref.of.lambda");

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef resolveTypeForParameter(CSharpLambdaExpressionImpl target, int parameterIndex)
	{
		CSharpLambdaResolveResult leftTypeRef = resolveLeftLambdaTypeRef(target);
		if(leftTypeRef == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		DotNetTypeRef[] leftTypeParameters = leftTypeRef.getParameterTypeRefs();
		DotNetTypeRef typeRef = ArrayUtil2.safeGet(leftTypeParameters, parameterIndex);
		return ObjectUtil.notNull(typeRef, DotNetTypeRef.UNKNOWN_TYPE);
	}

	@Nullable
	@RequiredReadAction
	public static CSharpLambdaResolveResult resolveLeftLambdaTypeRef(PsiElement target)
	{
		DotNetTypeRef typeRefOfLambda = target.getUserData(TYPE_REF_OF_LAMBDA);
		if(typeRefOfLambda != null)
		{
			DotNetTypeResolveResult typeResolveResult = typeRefOfLambda.resolve(target);
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return (CSharpLambdaResolveResult) typeResolveResult;
			}
			return null;
		}

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
			if(parent instanceof CSharpNamedCallArgument)
			{
				return null;
			}
			CSharpCallArgumentListOwner argumentListOwner = PsiTreeUtil.getParentOfType(parent, CSharpCallArgumentListOwner.class, false);
			if(argumentListOwner == null)
			{
				return null;
			}

			ResolveResult validOrFirstMaybeResult = CSharpResolveUtil.findValidOrFirstMaybeResult(argumentListOwner.multiResolve(false));
			if(validOrFirstMaybeResult == null)
			{
				return null;
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

						DotNetTypeResolveResult typeResolveResult = parameterTypeRef.resolve(target);
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
				DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(true).resolve(parent);
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
				}
			}
		}
		else if(parent instanceof CSharpTypeCastExpressionImpl)
		{
			DotNetTypeResolveResult typeResolveResult = ((CSharpTypeCastExpressionImpl) parent).toTypeRef(false).resolve(parent);
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
			DotNetTypeResolveResult typeResolveResult = methodAsElement.getReturnTypeRef().resolve(parent);
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return (CSharpLambdaResolveResult) typeResolveResult;
			}
		}
		else if(parent instanceof CSharpConditionalExpressionImpl)
		{
			DotNetExpression expression = ((CSharpConditionalExpressionImpl) parent).getTrueExpression();
			if(expression != null)
			{
				DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(false).resolve(parent);
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
				}
			}
			expression = ((CSharpConditionalExpressionImpl) parent).getFalseExpression();
			if(expression != null)
			{
				DotNetTypeResolveResult typeResolveResult = expression.toTypeRef(false).resolve(parent);
				if(typeResolveResult instanceof CSharpLambdaResolveResult)
				{
					return (CSharpLambdaResolveResult) typeResolveResult;
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
		DotNetTypeResolveResult typeResolveResult = leftTypeRef.resolve(variable);
		return typeResolveResult instanceof CSharpLambdaResolveResult ? (CSharpLambdaResolveResult) typeResolveResult : null;
	}
}
