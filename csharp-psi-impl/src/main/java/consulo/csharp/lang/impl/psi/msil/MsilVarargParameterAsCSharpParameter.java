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

package consulo.csharp.lang.impl.psi.msil;

import jakarta.annotation.Nonnull;

import consulo.language.impl.psi.LightElement;
import consulo.language.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpStaticTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

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

	@Nonnull
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
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
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
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return null;
	}
}
