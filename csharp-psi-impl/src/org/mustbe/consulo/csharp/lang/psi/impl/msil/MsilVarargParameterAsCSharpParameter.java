/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 03.02.15
 */
public class MsilVarargParameterAsCSharpParameter extends LightElement implements DotNetParameter
{
	private final DotNetParameterListOwner myParameterListOwner;

	public MsilVarargParameterAsCSharpParameter(DotNetParameterListOwner parameterListOwner)
	{
		super(parameterListOwner.getManager(), CSharpLanguage.INSTANCE);
		myParameterListOwner = parameterListOwner;
	}

	@Nullable
	@Override
	public DotNetParameterListOwner getOwner()
	{
		return myParameterListOwner;
	}

	@Override
	public int getIndex()
	{
		DotNetParameterList parameterList = myParameterListOwner.getParameterList();
		if(parameterList == null)
		{
			return 0;
		}
		return parameterList.getParametersCount();
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean b)
	{
		return CSharpStaticTypeRef.__ARGLIST_TYPE;
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return null;
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}
}
