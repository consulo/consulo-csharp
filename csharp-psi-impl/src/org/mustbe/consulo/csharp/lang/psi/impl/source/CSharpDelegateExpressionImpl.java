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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ExecuteTargetUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 19.01.14
 */
public class CSharpDelegateExpressionImpl extends CSharpElementImpl implements CSharpAnonymousMethodExpression, DotNetParameterListOwner
{
	public CSharpDelegateExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	public CSharpBlockStatementImpl getBodyStatement()
	{
		return findChildByClass(CSharpBlockStatementImpl.class);
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		DotNetParameterList parameterList = getParameterList();
		return parameterList == null ? DotNetTypeRef.EMPTY_ARRAY : parameterList.getParameterTypeRefs();
	}

	@Override
	@Nullable
	public DotNetParameterList getParameterList()
	{
		return findChildByClass(DotNetParameterList.class);
	}

	@Override
	@NotNull
	public DotNetParameter[] getParameters()
	{
		DotNetParameterList parameterList = getParameterList();
		return parameterList == null ? DotNetParameter.EMPTY_ARRAY : parameterList.getParameters();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitAnonymMethodExpression(this);
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent, @NotNull PsiElement
			place)
	{
		if(ExecuteTargetUtil.canProcess(processor, ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER))
		{
			for(DotNetParameter parameter : getParameters())
			{
				if(!processor.execute(parameter, state))
				{
					return false;
				}
			}
		}
		return true;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return new CSharpLambdaTypeRef(null, getParameterInfos(), DotNetTypeRef.AUTO_TYPE, getParameterList() == null);
	}

	@NotNull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		DotNetParameter[] parameters = getParameters();
		CSharpSimpleParameterInfo[] parameterInfos = new CSharpSimpleParameterInfo[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
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

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return getModifierElement(modifier) != null;
	}

	@Override
	@Nullable
	public PsiElement getModifierElement(@NotNull DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		switch(as)
		{
			case ASYNC:
				return findChildByType(CSharpSoftTokens.ASYNC_KEYWORD);
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return findChildByClass(DotNetStatement.class);
	}
}
