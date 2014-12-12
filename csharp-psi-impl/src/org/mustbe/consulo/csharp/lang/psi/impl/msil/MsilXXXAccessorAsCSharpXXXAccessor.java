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
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightElement;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 12.12.14
 */
public class MsilXXXAccessorAsCSharpXXXAccessor extends CSharpLightElement<DotNetXXXAccessor> implements DotNetXXXAccessor
{
	private final PsiElement myParent;
	private final MsilModifierListToCSharpModifierList myModifierList;

	public MsilXXXAccessorAsCSharpXXXAccessor(@NotNull PsiElement parent,
			@NotNull DotNetXXXAccessor original,
			@NotNull MsilMethodEntry resolvedMethod)
	{
		super(original);
		myParent = parent;
		myModifierList = new MsilModifierListToCSharpModifierList(resolvedMethod.getModifierList());
	}

	@Override
	public PsiElement getParent()
	{
		return myParent;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitXXXAccessor(this);
	}

	@Nullable
	@Override
	public PsiElement getAccessorElement()
	{
		return null;
	}

	@Nullable
	@Override
	public Kind getAccessorKind()
	{
		return myOriginal.getAccessorKind();
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return null;
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myModifierList.hasModifier(modifier);
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myModifierList;
	}

	@Override
	public String getName()
	{
		Kind accessorKind = getAccessorKind();
		return accessorKind == null ? "<unknown>" : accessorKind.name();
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
