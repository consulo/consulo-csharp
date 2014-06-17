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


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpEventUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpLambdaExpressionImplUtil
{
	@NotNull
	public static DotNetTypeRef resolveTypeForParameter(CSharpLambdaExpressionImpl lambdaExpression, int parameterIndex)
	{
		CSharpLambdaTypeRef leftTypeRef = resolveLeftLambdaTypeRef(lambdaExpression);
		if(leftTypeRef == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		DotNetTypeRef[] leftTypeParameters = leftTypeRef.getParameterTypes();
		DotNetTypeRef typeRef = ArrayUtil2.safeGet(leftTypeParameters, parameterIndex);
		return ObjectUtils.notNull(typeRef, DotNetTypeRef.UNKNOWN_TYPE);
	}

	@Nullable
	public static DotNetTypeRef fromVariable(DotNetVariable variable, int parameterIndex)
	{
		DotNetTypeRef leftTypeRef = variable.toTypeRef(false);
		if(!(leftTypeRef instanceof CSharpLambdaTypeRef))
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		DotNetTypeRef[] leftTypeParameters = ((CSharpLambdaTypeRef) leftTypeRef).getParameterTypes();
		return ArrayUtil2.safeGet(leftTypeParameters, parameterIndex);
	}

	@Nullable
	public static CSharpLambdaTypeRef resolveLeftLambdaTypeRef(CSharpLambdaExpressionImpl lambdaExpression)
	{
		PsiElement parent = lambdaExpression.getParent();
		if(parent instanceof DotNetVariable)
		{
			DotNetVariable variable = (DotNetVariable) parent;
			if(variable.getInitializer() != lambdaExpression)
			{
				return null;
			}
			return resolveLeftLambdaTypeRefForVariable(variable);
		}
		else if(parent instanceof CSharpCallArgumentList)
		{
			CSharpMethodCallExpressionImpl methodCallExpression = (CSharpMethodCallExpressionImpl) parent.getParent();
			DotNetExpression callExpression = methodCallExpression.getCallExpression();
			if(!(callExpression instanceof CSharpReferenceExpressionImpl))
			{
				return null;
			}
			PsiElement resolve = ((CSharpReferenceExpressionImpl) callExpression).resolve();
			if(!(resolve instanceof DotNetLikeMethodDeclaration))
			{
				return null;
			}
			DotNetExpression[] parameterExpressions = methodCallExpression.getParameterExpressions();
			int index = ArrayUtil.indexOf(parameterExpressions, lambdaExpression);
			if(index == -1)
			{
				return null;
			}

			return resolveLeftLambdaTypeRefForVariable(((DotNetLikeMethodDeclaration) resolve).getParameters()[index]);
		}

		DotNetEventDeclaration eventDeclaration = CSharpEventUtil.resolveEvent(parent);
		if(eventDeclaration != null)
		{
			return resolveLeftLambdaTypeRefForVariable(eventDeclaration);
		}

		return null;
	}

	@Nullable
	public static CSharpLambdaTypeRef resolveLeftLambdaTypeRefForVariable(DotNetVariable variable)
	{
		DotNetTypeRef leftTypeRef = variable.toTypeRef(false);
		if(!(leftTypeRef instanceof CSharpLambdaTypeRef))
		{
			return null;
		}
		return (CSharpLambdaTypeRef) leftTypeRef;
	}
}
