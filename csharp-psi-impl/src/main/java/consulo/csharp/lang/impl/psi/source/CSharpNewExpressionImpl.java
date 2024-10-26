/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.ast.IElementType;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpAnonymTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpNewExpressionImpl extends CSharpExpressionImpl implements CSharpNewExpression, CSharpArrayInitializerOwner
{
	public CSharpNewExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public boolean canResolve()
	{
		return getParameterList() != null;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNewExpression(this);
	}

	@RequiredReadAction
	@Nonnull
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
			Project project = getProject();
			GlobalSearchScope resolveScope = getResolveScope();
			if(canResolve())
			{
				DotNetTypeRef[] arguments = DotNetTypeRef.EMPTY_ARRAY;

				if(type instanceof CSharpUserType)
				{
					arguments = ((CSharpUserType) type).getReferenceExpression().getTypeArgumentListRefs();
					PsiElement psiElement = CSharpReferenceExpressionImplUtil.resolveByTypeKind(((CSharpUserType) type).getReferenceExpression(),
							false);
					typeRef = CSharpReferenceExpressionImplUtil.toTypeRef(resolveScope, psiElement);
				}
				else
				{
					typeRef = type.toTypeRef();
				}

				if(arguments.length != 0)
				{
					typeRef = new CSharpGenericWrapperTypeRef(project, resolveScope, typeRef, arguments);
				}
			}
			else
			{
				typeRef = type.toTypeRef();
			}

			for(CSharpNewArrayLengthImpl length : arrayLengths)
			{
				typeRef = new CSharpArrayTypeRef(project, resolveScope, typeRef, length.getDimensionSize());
			}
			return typeRef;
		}
	}

	@Nonnull
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
		return new CSharpArrayTypeRef(newExpression.getProject(), newExpression.getResolveScope(), firstItem, 0);
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

	@Nonnull
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

	@Nonnull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Nonnull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		CSharpCallArgument[] callArguments = getCallArguments();
		for(CSharpCallArgument callArgument : callArguments)
		{
			if(!callArgument.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
