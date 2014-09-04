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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ResolveResultWithWeight;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpArrayAccessExpressionImpl extends CSharpElementImpl implements DotNetExpression, CSharpCallArgumentListOwner, CSharpQualifiedNonReference
{
	public CSharpArrayAccessExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitArrayAccessExpression(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		PsiElement resolve = resolveToCallable();
		if(resolve instanceof CSharpArrayMethodDeclaration)
		{
			return ((CSharpArrayMethodDeclaration) resolve).getReturnTypeRef();
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	@NotNull
	public DotNetExpression getQualifier()
	{
		return (DotNetExpression) getFirstChild();
	}

	@Nullable
	@Override
	public String getReferenceName()
	{
		throw new IllegalArgumentException("This methid is never called");
	}

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Override
	public boolean canResolve()
	{
		return true;
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
	}

	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		return DotNetTypeRef.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		ResolveResult[] resolveResults = multiResolve(false);
		if(resolveResults.length == 0)
		{
			return null;
		}
		ResolveResultWithWeight resolveResult = (ResolveResultWithWeight) resolveResults[0];
		if(!resolveResult.isGoodResult())
		{
			return null;
		}
		return resolveResult.getElement();
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		ResolveResult[] resolveResults = CSharpReferenceExpressionImpl.multiResolve0(CSharpReferenceExpressionImpl.ResolveToKind.ARRAY_METHOD, this,
				this);
		if(!incompleteCode)
		{
			return resolveResults;
		}
		List<ResolveResultWithWeight> filter = new ArrayList<ResolveResultWithWeight>();
		for(ResolveResult resolveResult : resolveResults)
		{
			ResolveResultWithWeight resolveResultWithWeight = (ResolveResultWithWeight) resolveResult;
			if(resolveResultWithWeight.isGoodResult())
			{
				filter.add(resolveResultWithWeight);
			}
		}
		return ContainerUtil.toArray(filter, ResolveResultWithWeight.ARRAY_FACTORY);
	}
}
