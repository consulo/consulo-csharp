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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
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
	public static DotNetTypeRef resolveTypeForParameter(PsiElement target, int parameterIndex)
	{
		CSharpLambdaResolveResult leftTypeRef = resolveLeftLambdaTypeRef(target);
		if(leftTypeRef == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		DotNetTypeRef[] leftTypeParameters = leftTypeRef.getParameterTypeRefs();
		DotNetTypeRef typeRef = ArrayUtil2.safeGet(leftTypeParameters, parameterIndex);
		return ObjectUtils.notNull(typeRef, DotNetTypeRef.UNKNOWN_TYPE);
	}

	@Nullable
	public static CSharpLambdaResolveResult resolveLeftLambdaTypeRef(PsiElement target)
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
			int index = ArrayUtil.indexOf(parameterExpressions, target);
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
	public static CSharpLambdaResolveResult resolveLeftLambdaTypeRefForVariable(DotNetVariable variable)
	{
		DotNetTypeRef leftTypeRef = variable.toTypeRef(false);
		DotNetTypeResolveResult typeResolveResult = leftTypeRef.resolve(variable);
		return typeResolveResult instanceof CSharpLambdaResolveResult ? (CSharpLambdaResolveResult) typeResolveResult : null;
	}
}
