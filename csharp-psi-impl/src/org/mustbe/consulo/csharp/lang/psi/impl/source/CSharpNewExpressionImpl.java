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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySetBlock;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpAnonymTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
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
		CSharpNewArrayLengthImpl[] arrayLengths = getNewArrayLengths();
		DotNetType type = getNewType();
		if(type == null)
		{
			if(arrayLengths.length == 1)
			{
				CSharpArrayInitializationExpressionImpl arrayInitializationExpression = getArrayInitializationExpression();
				if(arrayInitializationExpression == null)
				{
					return DotNetTypeRef.ERROR_TYPE;
				}

				return arrayInitializationExpression.toTypeRef(resolveFromParent);
			}

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
					typeRef = new CSharpGenericWrapperTypeRef(typeRef, DotNetTypeRefUtil.toArray(arguments));
				}
			}
			else
			{
				typeRef = type.toTypeRef();
			}

			for(CSharpNewArrayLengthImpl length : arrayLengths)
			{
				typeRef = new CSharpArrayTypeRef(typeRef, length.getDimensionSize());
			}
			return typeRef;
		}
	}

	@Nullable
	public CSharpArrayInitializationExpressionImpl getArrayInitializationExpression()
	{
		return findChildByClass(CSharpArrayInitializationExpressionImpl.class);
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
		if(!canResolve())
		{
			return null;
		}
		DotNetReferenceExpression expressionForResolving = getExpressionForResolving();
		return expressionForResolving != null ? expressionForResolving.resolve() : null;
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		if(!canResolve())
		{
			return ResolveResult.EMPTY_ARRAY;
		}
		DotNetReferenceExpression expressionForResolving = getExpressionForResolving();
		return expressionForResolving != null ? expressionForResolving.multiResolve(incompleteCode) : ResolveResult.EMPTY_ARRAY;
	}

	private DotNetReferenceExpression getExpressionForResolving()
	{
		DotNetType newType = getNewType();
		if(newType instanceof DotNetUserType)
		{
			DotNetReferenceExpression referenceExpression = ((DotNetUserType) newType).getReferenceExpression();
			if(referenceExpression instanceof CSharpReferenceExpression)
			{
				return referenceExpression;
			}
		}
		else if(newType instanceof DotNetTypeWithTypeArguments)
		{
			DotNetType c = ((DotNetTypeWithTypeArguments) newType).getInnerType();
			if(c instanceof DotNetUserType)
			{
				DotNetReferenceExpression referenceExpression = ((DotNetUserType) c).getReferenceExpression();
				if(referenceExpression instanceof CSharpReferenceExpression)
				{
					return referenceExpression;
				}
			}
		}
		return null;
	}
	@NotNull
	@Override
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
}
