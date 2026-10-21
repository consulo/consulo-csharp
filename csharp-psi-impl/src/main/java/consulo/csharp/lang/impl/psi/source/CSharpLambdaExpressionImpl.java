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

import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;

import consulo.csharp.lang.impl.psi.CSharpTypeUtil;

import consulo.csharp.lang.CSharpCastType;

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

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLambdaExpressionImpl extends CSharpExpressionImpl implements CSharpAnonymousMethodExpression
{
	public CSharpLambdaExpressionImpl(IElementType elementType)
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
	public boolean hasModifier(DotNetModifier modifier)
	{
		return getModifierList().hasModifier(modifier);
	}

	@RequiredReadAction
	@Override
	public DotNetModifierList getModifierList()
	{
		return CachedValuesManager.getManager(getProject()).createCachedValue(() -> CachedValueProvider.Result.<DotNetModifierList>create(new CSharpAnonymousModifierListImpl
				(CSharpLambdaExpressionImpl.this), CSharpLambdaExpressionImpl.this), false).getValue();
	}

	@Override
	public CSharpCodeBodyProxy getCodeBlock()
	{
		return new CSharpCodeBodyProxyImpl(this);
	}

	@RequiredReadAction
	public PsiElement getDArrow()
	{
		return findNotNullChildByType(CSharpTokens.DARROW);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitLambdaExpression(this);
	}

	@Nullable
	public CSharpLambdaParameterList getParameterList()
	{
		return findChildByClass(CSharpLambdaParameterList.class);
	}

	public CSharpLambdaParameter[] getParameters()
	{
		CSharpLambdaParameterList parameterList = getParameterList();
		return parameterList == null ? CSharpLambdaParameterImpl.EMPTY_ARRAY : parameterList.getParameters();
	}

	@Override
	public boolean processDeclarations(PsiScopeProcessor processor, ResolveState state, PsiElement lastParent, PsiElement place)
	{
		CSharpLambdaParameter[] parameters = getParameters();
		// (_, _) => ... - more than one parameter named _ makes them all discards, which are not in scope (C# 9)
		long underscores = Arrays.stream(parameters).filter(parameter -> "_".equals(parameter.getName())).count();
		for(CSharpLambdaParameter parameter : parameters)
		{
			if(underscores > 1 && "_".equals(parameter.getName()))
			{
				continue;
			}
			if(!processor.execute(parameter, state))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
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
			public void visitAnonymousMethodExpression(CSharpDelegateExpressionImpl method)
			{
				// We don't need to check return inside anonymous method
			}

			@Override
			public void visitLambdaExpression(CSharpLambdaExpressionImpl expression)
			{
				// We don't need to check return inside lambda
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
		return bestCommonType(typeRefs);
	}

	/**
	 * The type every returned type converts to - {@code return null; return c.Name;} returns string (C# spec 12.6.3.15).
	 */
	@RequiredReadAction
	private DotNetTypeRef bestCommonType(List<DotNetTypeRef> typeRefs)
	{
		for(DotNetTypeRef candidate : typeRefs)
		{
			// the null literal has no type of its own to be the common one
			if(candidate instanceof CSharpNullTypeRef)
			{
				continue;
			}

			boolean allConvert = true;
			for(DotNetTypeRef other : typeRefs)
			{
				// null converts to any reference or nullable type
				if(other instanceof CSharpNullTypeRef)
				{
					continue;
				}
				if(other != candidate && !CSharpTypeUtil.isTypeEqual(other, candidate)
						&& !CSharpTypeUtil.isInheritable(candidate, other, CSharpCastType.IMPLICIT, getResolveScope()).isSuccess())
				{
					allConvert = false;
					break;
				}
			}
			if(allConvert)
			{
				return candidate;
			}
		}
		return typeRefs.get(0);
	}

	@RequiredReadAction
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return getParameterInfos(false);
	}

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
