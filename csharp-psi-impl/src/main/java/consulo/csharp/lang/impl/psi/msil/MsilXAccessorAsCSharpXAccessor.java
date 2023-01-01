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

import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetCodeBodyProxy;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.language.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12.12.14
 */
public class MsilXAccessorAsCSharpXAccessor extends MsilElementWrapper<DotNetXAccessor> implements DotNetXAccessor
{
	private final PsiElement myParent;
	private final MsilModifierListToCSharpModifierList myModifierList;

	public MsilXAccessorAsCSharpXAccessor(@Nonnull PsiElement parent,
										  @Nonnull DotNetXAccessor original,
										  @Nonnull MsilMethodEntry resolvedMethod)
	{
		super(parent, original);
		myParent = parent;
		myModifierList = new MsilModifierListToCSharpModifierList(this, resolvedMethod.getModifierList());
	}

	@Override
	public PsiElement getParent()
	{
		return myParent;
	}

	@Override
	public String toString()
	{
		return "DotNetXAccessor: " + getAccessorKind();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitXAccessor(this);
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

	@Nonnull
	@Override
	public DotNetCodeBodyProxy getCodeBlock()
	{
		return CSharpCodeBodyProxy.EMPTY;
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
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
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return DotNetXAccessor.class;
	}
}
