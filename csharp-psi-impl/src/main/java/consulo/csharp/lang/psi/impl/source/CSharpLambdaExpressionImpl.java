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

package consulo.csharp.lang.psi.impl.source;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpLambdaParameterList;
import consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import consulo.csharp.lang.psi.impl.source.resolve.genericInference.GenericInferenceUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLambdaExpressionImpl extends CSharpExpressionImpl implements CSharpAnonymousMethodExpression
{
	public CSharpLambdaExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public DotNetTypeRef getInferenceSessionTypeRef()
	{
		GenericInferenceUtil.InferenceSessionData inferenceSessionData = GenericInferenceUtil.ourInsideInferenceSession.get();
		return inferenceSessionData != null ? inferenceSessionData.getTypeRef(this) : null;
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

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		DotNetExpression singleExpression = findChildByClass(DotNetExpression.class);
		if(singleExpression != null)
		{
			return singleExpression;
		}
		return findChildByClass(DotNetStatement.class);
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
		return new CSharpLambdaTypeRef(this, null, getParameterInfos(resolveFromParent), resolveFromParent ? getReturnTypeRef() : DotNetTypeRef.AUTO_TYPE);
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
		return new CSharpLambdaTypeRef(this, null, getParameterInfos(true), returnType);
	}

	@Nonnull
	@RequiredReadAction
	private DotNetTypeRef findPossibleReturnTypeRef()
	{
		PsiElement codeBlock = getCodeBlock();
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
				CSharpImplicitReturnModel implicitReturnModel = CSharpImplicitReturnModel.getImplicitReturnModel(statement, CSharpLambdaExpressionImpl.this);

				DotNetExpression expression = statement.getExpression();
				DotNetTypeRef expectedTypeRef;

				expectedTypeRef = expression == null ? new CSharpTypeRefByQName(statement, DotNetTypes.System.Void) : expression.toTypeRef(false);
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
					if(DotNetTypeRefUtil.isVmQNameEqual(expectedTypeRef, statement, DotNetTypes.System.Void))
					{
						typeRefs.add(new CSharpTypeRefByQName(statement, implicitReturnModel.getNoGenericTypeVmQName()));
					}
					else
					{
						typeRefs.add(new CSharpGenericWrapperTypeRef(statement.getProject(), new CSharpTypeRefByQName(statement, implicitReturnModel.getGenericVmQName()), expectedTypeRef));
					}
				}
			}
		});

		if(typeRefs.isEmpty())
		{
			return new CSharpTypeRefByQName(this, DotNetTypes.System.Void);
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
