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
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpAttributeImpl extends CSharpElementImpl implements CSharpAttribute
{
	public CSharpAttributeImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitAttribute(this);
	}

	@Nullable
	@Override
	public DotNetTypeDeclaration resolveToType()
	{
		CSharpReferenceExpression ref = findChildByClass(CSharpReferenceExpression.class);
		if(ref == null)
		{
			return null;
		}

		PsiElement psiElement = CSharpReferenceExpressionImplUtil.resolveByTypeKind(ref, true);
		if(psiElement instanceof DotNetTypeDeclaration)
		{
			return (DotNetTypeDeclaration) psiElement;
		}
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		DotNetTypeDeclaration dotNetTypeDeclaration = resolveToType();
		if(dotNetTypeDeclaration != null)
		{
			return new CSharpTypeRefFromQualifiedElement(dotNetTypeDeclaration);
		}
		return DotNetTypeRef.ERROR_TYPE;
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

	@Override
	@NotNull
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		CSharpReferenceExpression ref = getReferenceExpression();
		if(ref == null)
		{
			return null;
		}

		return ref.resolve();
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		CSharpReferenceExpression ref = getReferenceExpression();
		if(ref == null)
		{
			return null;
		}

		return ref.multiResolve(incompleteCode);
	}

	@Override
	@Nullable
	public CSharpReferenceExpression getReferenceExpression()
	{
		return findChildByClass(CSharpReferenceExpression.class);
	}
}
