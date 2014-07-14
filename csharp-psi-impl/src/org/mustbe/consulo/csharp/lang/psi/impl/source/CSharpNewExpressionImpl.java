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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySetBlock;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpAnonymTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetTypeWithTypeArguments;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpNewExpressionImpl extends CSharpElementImpl implements CSharpNewExpression
{
	public CSharpNewExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public boolean canResolve()
	{
		return getParameterList() != null;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNewExpression(this);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		DotNetType type = getNewType();
		if(type == null)
		{
			CSharpFieldOrPropertySetBlock fieldOrPropertySetBlock = getFieldOrPropertySetBlock();
			if(fieldOrPropertySetBlock == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}
			return new CSharpAnonymTypeRef(getContainingFile(), fieldOrPropertySetBlock.getSets());
		}
		else
		{
			DotNetTypeRef typeRef;
			if(canResolve())
			{
				DotNetType[] arguments = DotNetType.EMPTY_ARRAY;
				if(type instanceof DotNetTypeWithTypeArguments)
				{
					arguments = ((DotNetTypeWithTypeArguments) type).getArguments();
					type = ((DotNetTypeWithTypeArguments) type).getInnerType();
				}

				if(type instanceof DotNetUserType)
				{
					PsiElement psiElement = CSharpReferenceExpressionImplUtil.resolveByTypeKind(((DotNetUserType) type).getReferenceExpression(),
							false);
					typeRef = CSharpReferenceExpressionImpl.toTypeRef(psiElement);
				}
				else
				{
					typeRef = type.toTypeRef();
				}

				if(arguments.length != 0)
				{
					typeRef = new DotNetGenericWrapperTypeRef(typeRef, DotNetTypeRefUtil.toArray(arguments));
				}
			}
			else
			{
				typeRef = type.toTypeRef();
			}

			for(CSharpNewArrayLengthImpl length : getNewArrayLengths())
			{
				typeRef = new CSharpArrayTypeRef(typeRef, length.getDimensionSize());
			}
			return typeRef;
		}
	}

	public CSharpNewArrayLengthImpl[] getNewArrayLengths()
	{
		return findChildrenByClass(CSharpNewArrayLengthImpl.class);
	}

	@Nullable
	@Override
	public DotNetType getNewType()
	{
		return findChildByClass(DotNetType.class);
	}

	@Nullable
	@Override
	public CSharpFieldOrPropertySetBlock getFieldOrPropertySetBlock()
	{
		return findChildByClass(CSharpFieldOrPropertySetBlock.class);
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

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return null;
	}

	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		if(!canResolve())
		{
			return ResolveResult.EMPTY_ARRAY;
		}
		DotNetType newType = getNewType();
		if(newType instanceof DotNetUserType)
		{
			DotNetReferenceExpression referenceExpression = ((DotNetUserType) newType).getReferenceExpression();
			if(referenceExpression instanceof CSharpReferenceExpression)
			{
				return referenceExpression.multiResolve(incompleteCode);
			}
		}
		return ResolveResult.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}
}
