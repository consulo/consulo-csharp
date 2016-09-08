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

package consulo.csharp.lang.psi.impl.source;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySetBlock;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpAnonymTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNullTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpNewExpressionImpl extends CSharpExpressionImpl implements CSharpNewExpression, CSharpArrayInitializerOwner
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

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		CSharpNewArrayLengthImpl[] arrayLengths = getNewArrayLengths();
		DotNetType type = getNewType();
		if(type == null)
		{
			if(arrayLengths.length == 1)
			{
				CSharpArrayInitializerImpl arrayInitializer = getArrayInitializer();
				if(arrayInitializer == null)
				{
					return DotNetTypeRef.ERROR_TYPE;
				}

				return calcType(this, arrayInitializer.getValues());
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
				DotNetTypeRef[] arguments = DotNetTypeRef.EMPTY_ARRAY;

				if(type instanceof CSharpUserType)
				{
					arguments = ((CSharpUserType) type).getReferenceExpression().getTypeArgumentListRefs();
					PsiElement psiElement = CSharpReferenceExpressionImplUtil.resolveByTypeKind(((CSharpUserType) type).getReferenceExpression(),
							false);
					typeRef = CSharpReferenceExpressionImplUtil.toTypeRef(psiElement);
				}
				else
				{
					typeRef = type.toTypeRef();
				}

				if(arguments.length != 0)
				{
					typeRef = new CSharpGenericWrapperTypeRef(typeRef, arguments);
				}
			}
			else
			{
				typeRef = type.toTypeRef();
			}

			for(CSharpNewArrayLengthImpl length : arrayLengths)
			{
				typeRef = new CSharpArrayTypeRef(this, typeRef, length.getDimensionSize());
			}
			return typeRef;
		}
	}

	@NotNull
	private static DotNetTypeRef calcType(CSharpNewExpressionImpl newExpression, CSharpArrayInitializerValue[] values)
	{
		if(values.length == 0)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		Set<DotNetTypeRef> typeRefs = new LinkedHashSet<DotNetTypeRef>();
		for(int i = 0; i < values.length; i++)
		{
			CSharpArrayInitializerValue value = values[i];

			if(value instanceof CSharpArrayInitializerCompositeValueImpl)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}
			else if(value instanceof CSharpArrayInitializerSingleValueImpl)
			{
				DotNetExpression expression = ((CSharpArrayInitializerSingleValueImpl) value).getArgumentExpression();
				if(expression == null)
				{
					continue;
				}

				typeRefs.add(expression.toTypeRef(true));
			}
		}

		for(Iterator<DotNetTypeRef> iterator = typeRefs.iterator(); iterator.hasNext(); )
		{
			DotNetTypeRef next = iterator.next();
			if(next instanceof CSharpNullTypeRef)
			{
				iterator.remove();
			}
		}

		if(typeRefs.isEmpty())
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		//TODO [VISTALL] better calc
		DotNetTypeRef firstItem = ContainerUtil.getFirstItem(typeRefs);
		return new CSharpArrayTypeRef(newExpression, firstItem, 0);
	}

	@Override
	@Nullable
	public CSharpArrayInitializerImpl getArrayInitializer()
	{
		return findChildByClass(CSharpArrayInitializerImpl.class);
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

	@Nullable
	private CSharpReferenceExpression getExpressionForResolving()
	{
		DotNetType newType = getNewType();
		if(newType instanceof CSharpUserType)
		{
			return ((CSharpUserType) newType).getReferenceExpression();
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
