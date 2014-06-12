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
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameterList;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLambdaExpressionImpl extends CSharpElementImpl implements DotNetExpression, CSharpPseudoMethod
{
	public CSharpLambdaExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
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
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent,
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
		CSharpLambdaParameter[] parameters = getParameters();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			CSharpLambdaParameter parameter = parameters[i];
			typeRefs[i] = parameter.toTypeRef(resolveFromParent);
		}
		return new CSharpLambdaTypeRef(null, typeRefs, resolveFromParent ? getReturnTypeRef() : DotNetTypeRef.AUTO_TYPE);
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		CSharpLambdaParameter[] parameters = getParameters();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			CSharpLambdaParameter parameter = parameters[i];
			typeRefs[i] = parameter.toTypeRef(false);
		}
		return typeRefs;
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		CSharpLambdaTypeRef type = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(this);
		if(type == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		return type.getReturnType();
	}
}
