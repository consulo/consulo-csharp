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
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class CSharpMethodCallExpressionImpl extends CSharpElementImpl implements DotNetExpression, CSharpCallArgumentListOwner
{
	public CSharpMethodCallExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public boolean canResolve()
	{
		return true;
	}

	@Override
	@Nullable
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
	}

	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return (DotNetTypeList) findChildByType(CSharpElements.TYPE_CALL_ARGUMENTS);
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		DotNetTypeList typeArgumentList = getTypeArgumentList();
		return typeArgumentList == null ? DotNetTypeRef.EMPTY_ARRAY : typeArgumentList.getTypeRefs();
	}

	@Override
	@NotNull
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@NotNull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}

	@NotNull
	public DotNetExpression getCallExpression()
	{
		return findNotNullChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitMethodCallExpression(this);
	}

	@Override
	@Nullable
	public PsiElement resolveToCallable()
	{
		DotNetExpression callExpression = getCallExpression();

		if(callExpression instanceof CSharpReferenceExpressionImpl)
		{
			PsiElement resolvedElement = ((CSharpReferenceExpressionImpl) callExpression).resolve();
			if(resolvedElement != null)
			{
				return resolvedElement;
			}

			ResolveResult[] resolveResults = multiResolve(false);
			if(resolveResults.length > 0)
			{
				return resolveResults[0].getElement();
			}
		}
		return null;
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		DotNetExpression callExpression = getCallExpression();

		if(callExpression instanceof CSharpReferenceExpressionImpl)
		{
			return ((CSharpReferenceExpressionImpl) callExpression).multiResolve(incompleteCode);
		}
		return ResolveResult.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		PsiElement resolvedElement = resolveToCallable();
		if(resolvedElement instanceof DotNetVariable)
		{
			DotNetTypeRef dotNetTypeRef = ((DotNetVariable) resolvedElement).toTypeRef(false);
			DotNetTypeResolveResult typeResolveResult = dotNetTypeRef.resolve(this);
			if(typeResolveResult instanceof CSharpLambdaResolveResult)
			{
				return ((CSharpLambdaResolveResult) typeResolveResult).getReturnTypeRef();
			}
		}
		if(resolvedElement instanceof CSharpSimpleLikeMethodAsElement)
		{
			return ((CSharpSimpleLikeMethodAsElement) resolvedElement).getReturnTypeRef();
		}
		return CSharpReferenceExpressionImpl.toTypeRef(resolvedElement);
	}
}
