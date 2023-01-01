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

import consulo.application.util.CachedValuesManager;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.application.util.RecursionManager;
import consulo.language.psi.resolve.ResolveState;
import consulo.application.util.CachedValueProvider;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.CSharpImplicitReturnModel;
import consulo.csharp.lang.impl.psi.source.resolve.genericInference.GenericInferenceManager;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLambdaExpressionImpl extends CSharpExpressionImpl implements CSharpAnonymousMethodExpression
{
	public CSharpLambdaExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nullable
	public DotNetTypeRef getInferenceSessionTypeRef()
	{
		return GenericInferenceManager.getInstance(getProject()).getInferenceSessionTypeRef(this);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return getModifierList().hasModifier(modifier);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetModifierList getModifierList()
	{
		return CachedValuesManager.getManager(getProject()).createCachedValue(() -> CachedValueProvider.Result.<DotNetModifierList>create(new CSharpAnonymousModifierListImpl
				(CSharpLambdaExpressionImpl.this), CSharpLambdaExpressionImpl.this), false).getValue();
	}

	@Nonnull
	@Override
	public CSharpCodeBodyProxy getCodeBlock()
	{
		return new CSharpCodeBodyProxyImpl(this);
	}

	@Nonnull
	@RequiredReadAction
	public PsiElement getDArrow()
	{
		return findNotNullChildByType(CSharpTokens.DARROW);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaExpression(this);
	}

	@Nullable
	public CSharpLambdaParameterList getParameterList()
	{
		return findChildByClass(CSharpLambdaParameterList.class);
	}

	@Nonnull
	public CSharpLambdaParameter[] getParameters()
	{
		CSharpLambdaParameterList parameterList = getParameterList();
		return parameterList == null ? CSharpLambdaParameterImpl.EMPTY_ARRAY : parameterList.getParameters();
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		for(CSharpLambdaParameter parameter : getParameters())
		{
			if(!processor.execute(parameter, state))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetTypeRef forceTypeRef = getInferenceSessionTypeRef();
		if(forceTypeRef != null)
		{
			return forceTypeRef;
		}
		return new CSharpLambdaTypeRef(getProject(), getResolveScope(), null, getParameterInfos(resolveFromParent), resolveFromParent ? getReturnTypeRef() : DotNetTypeRef.AUTO_TYPE);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef toTypeRefForInference()
	{
		// recursion when child lambda reference to parameter from parent lambda
		DotNetTypeRef returnType = RecursionManager.doPreventingRecursion("C# lambda return type", false, this::findPossibleReturnTypeRef);
		if(returnType == null)
		{
			returnType = DotNetTypeRef.ERROR_TYPE;
		}
		return new CSharpLambdaTypeRef(getProject(), getResolveScope(), null, getParameterInfos(true), returnType);
	}

	@Nonnull
	@RequiredReadAction
	private DotNetTypeRef findPossibleReturnTypeRef()
	{
		PsiElement codeBlock = getCodeBlock().getElement();
		if(codeBlock instanceof DotNetExpression)
		{
			return ((DotNetExpression) codeBlock).toTypeRef(false);
		}

		if(codeBlock == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		final List<DotNetTypeRef> typeRefs = new ArrayList<>();
		codeBlock.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitAnonymMethodExpression(CSharpDelegateExpressionImpl method)
			{
				// dont need check return inside anonym
			}

			@Override
			public void visitLambdaExpression(CSharpLambdaExpressionImpl expression)
			{
				// dont need check return inside lambda
			}

			@Override
			public void visitYieldStatement(CSharpYieldStatementImpl statement)
			{
				//FIXME [VISTALL] what we need to do?
			}

			@Override
			@RequiredReadAction
			public void visitReturnStatement(CSharpReturnStatementImpl statement)
			{
				Project project = statement.getProject();
				GlobalSearchScope resolveScope = statement.getResolveScope();
				
				CSharpImplicitReturnModel implicitReturnModel = CSharpImplicitReturnModel.getImplicitReturnModel(statement, CSharpLambdaExpressionImpl.this);

				DotNetExpression expression = statement.getExpression();
				DotNetTypeRef expectedTypeRef;

				expectedTypeRef = expression == null ? new CSharpTypeRefByQName(project, resolveScope, DotNetTypes.System.Void) : expression.toTypeRef(false);
				if(expectedTypeRef == DotNetTypeRef.ERROR_TYPE)
				{
					return;
				}

				if(implicitReturnModel == CSharpImplicitReturnModel.None)
				{
					typeRefs.add(expectedTypeRef);
				}
				else
				{
					if(DotNetTypeRefUtil.isVmQNameEqual(expectedTypeRef, DotNetTypes.System.Void))
					{
						typeRefs.add(new CSharpTypeRefByQName(project, resolveScope, implicitReturnModel.getNoGenericTypeVmQName()));
					}
					else
					{
						typeRefs.add(new CSharpGenericWrapperTypeRef(project, resolveScope, new CSharpTypeRefByQName(project, resolveScope, implicitReturnModel.getGenericVmQName()), expectedTypeRef));
					}
				}
			}
		});

		if(typeRefs.isEmpty())
		{
			return new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes.System.Void);
		}
		return typeRefs.get(0);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return getParameterInfos(false);
	}

	@Nonnull
	@RequiredReadAction
	public CSharpSimpleParameterInfo[] getParameterInfos(boolean resolveFromParent)
	{
		CSharpLambdaParameter[] parameters = getParameters();
		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			CSharpLambdaParameter parameter = parameters[i];
			parameterInfos[i] = new CSharpSimpleParameterInfo(i, parameter.getName(), parameter, parameter.toTypeRef(resolveFromParent));
		}
		return parameterInfos;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		CSharpLambdaResolveResult type = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(this);
		if(type == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		return type.getReturnTypeRef();
	}
}
