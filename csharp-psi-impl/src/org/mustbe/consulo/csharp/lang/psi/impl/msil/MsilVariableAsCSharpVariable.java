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
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.ModifierElementType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilVariableAsCSharpVariable extends LightElement implements DotNetVariable
{
	private final DotNetVariable myVariable;

	public MsilVariableAsCSharpVariable(DotNetVariable variable)
	{
		super(PsiManager.getInstance(variable.getProject()), CSharpLanguage.INSTANCE);
		myVariable = variable;
	}

	@Override
	public boolean isConstant()
	{
		return false;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return MsilToCSharpUtil.extractToCSharp(myVariable.toTypeRef(resolveFromInitializer), myVariable);
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
		ModifierElementType modifierElementType = MsilToCSharpUtil.toMsilModifier(modifier);
		if(modifierElementType == null)
		{
			return false;
		}
		return myVariable.hasModifier(modifierElementType);
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myVariable.getModifierList();
	}

	@Override
	public String toString()
	{
		return getName();
	}

	@Override
	public String getName()
	{
		return myVariable.getName();
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
