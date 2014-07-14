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
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
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
		return (DotNetTypeList) findChildByType(CSharpElements.TYPE_ARGUMENTS);
	}

	@Override
	@NotNull
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
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
			return ((CSharpReferenceExpressionImpl) callExpression).resolve();
		}
		return null;
	}

	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		DotNetExpression callExpression = getCallExpression();

		if(callExpression instanceof CSharpReferenceExpressionImpl)
		{
			return ((CSharpReferenceExpressionImpl) callExpression).multiResolve(incompleteCode);
		}
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		PsiElement resolve = resolveToCallable();
		if(resolve instanceof DotNetVariable)
		{
			DotNetTypeRef dotNetTypeRef = ((DotNetVariable) resolve).toTypeRef(false);
			if(dotNetTypeRef instanceof CSharpLambdaTypeRef)
			{
				return ((CSharpLambdaTypeRef) dotNetTypeRef).getReturnType();
			}
		}
		if(resolve instanceof CSharpPseudoMethod)
		{
			return ((CSharpPseudoMethod) resolve).getReturnTypeRef();
		}
		return CSharpReferenceExpressionImpl.toTypeRef(resolve);
	}
}
