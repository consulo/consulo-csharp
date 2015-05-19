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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public abstract class MsilVariableAsCSharpVariable extends MsilElementWrapper<DotNetVariable> implements DotNetVariable
{
	private MsilModifierListToCSharpModifierList myModifierList;
	private NotNullLazyValue<DotNetTypeRef> myVariableTypRefValue = new NotNullLazyValue<DotNetTypeRef>()
	{
		@NotNull
		@Override
		protected DotNetTypeRef compute()
		{
			return toTypeRefImpl();
		}
	};

	public MsilVariableAsCSharpVariable(PsiElement parent, DotNetVariable variable)
	{
		this(parent, CSharpModifier.EMPTY_ARRAY, variable);
	}

	public MsilVariableAsCSharpVariable(PsiElement parent, CSharpModifier[] modifiers, DotNetVariable variable)
	{
		super(parent, variable);
		setNavigationElement(variable);
		myModifierList = createModifierList(modifiers, variable);
	}

	@NotNull
	protected MsilModifierListToCSharpModifierList createModifierList(CSharpModifier[] modifiers, DotNetVariable variable)
	{
		return new MsilModifierListToCSharpModifierList(modifiers, variable, variable.getModifierList());
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myOriginal.getContainingFile();
	}

	public DotNetVariable getVariable()
	{
		return myOriginal;
	}

	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@Override
	@RequiredReadAction
	public boolean isConstant()
	{
		return false;
	}

	@NotNull
	@Override
	public final DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return myVariableTypRefValue.getValue();
	}

	@NotNull
	public DotNetTypeRef toTypeRefImpl()
	{
		return MsilToCSharpUtil.extractToCSharp(myOriginal.toTypeRef(false), myOriginal);
	}

	@RequiredReadAction
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

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myModifierList.hasModifier(modifier);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myModifierList;
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}
}
