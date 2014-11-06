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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameterList;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpRecursiveElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import lombok.val;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLambdaExpressionImpl extends CSharpElementImpl implements DotNetExpression, CSharpSimpleLikeMethodAsElement
{
	public CSharpLambdaExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public DotNetExpression getSingleExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Nullable
	public CSharpBlockStatementImpl getBlockStatement()
	{
		return findChildByClass(CSharpBlockStatementImpl.class);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaExpression(this);
	}

	@Nullable
	public CSharpLambdaParameterList getParameterList()
	{
		return findChildByClass(CSharpLambdaParameterList.class);
	}

	@NotNull
	public CSharpLambdaParameter[] getParameters()
	{
		CSharpLambdaParameterList parameterList = getParameterList();
		return parameterList == null ? CSharpLambdaParameterImpl.EMPTY_ARRAY : parameterList.getParameters();
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
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

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return new CSharpLambdaTypeRef(null, getParameterInfos(), resolveFromParent ? getReturnTypeRef() : findPossibleReturnTypeRef());
	}

	@NotNull
	private DotNetTypeRef findPossibleReturnTypeRef()
	{
		DotNetExpression singleExpression = getSingleExpression();
		if(singleExpression != null)
		{
			DotNetTypeRef dotNetTypeRef = singleExpression.toTypeRef(false);
			return dotNetTypeRef == DotNetTypeRef.ERROR_TYPE ? DotNetTypeRef.AUTO_TYPE : dotNetTypeRef;
		}

		CSharpBlockStatementImpl blockStatement = getBlockStatement();
		if(blockStatement == null)
		{
			return DotNetTypeRef.AUTO_TYPE;
		}

		val typeRefs = new ArrayList<DotNetTypeRef>();
		blockStatement.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitAnonymMethodExpression(CSharpAnonymMethodExpressionImpl method)
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
			public void visitReturnStatement(CSharpReturnStatementImpl statement)
			{
				DotNetExpression expression = statement.getExpression();
				if(expression == null)
				{
					typeRefs.add(new CSharpTypeRefByQName(DotNetTypes.System.Void));
				}
				else
				{
					DotNetTypeRef ref = expression.toTypeRef(false);
					if(ref == DotNetTypeRef.ERROR_TYPE)
					{
						return;
					}
					typeRefs.add(ref);
				}
			}
		});

		if(typeRefs.isEmpty())
		{
			return DotNetTypeRef.AUTO_TYPE;
		}
		return typeRefs.get(0);
	}

	@NotNull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		CSharpLambdaParameter[] parameters = getParameters();
		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			CSharpLambdaParameter parameter = parameters[i];
			parameterInfos[i] = new CSharpSimpleParameterInfo(i, parameter.getName(), parameter, parameter.toTypeRef(false));
		}
		return parameterInfos;
	}

	@NotNull
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
