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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.language.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpIdentifier;
import consulo.csharp.lang.psi.CSharpNamedElement;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.language.psi.PsiNameIdentifierOwner;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public abstract class CSharpMemberImpl extends CSharpElementImpl implements PsiNameIdentifierOwner, DotNetModifierListOwner, CSharpNamedElement
{
	public CSharpMemberImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@RequiredReadAction
	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier != null ? nameIdentifier.getTextOffset() : super.getTextOffset();
	}

	@RequiredReadAction
	@Override
	@Nullable
	public DotNetModifierList getModifierList()
	{
		return findChildByClass(DotNetModifierList.class);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		DotNetModifierList modifierList = getModifierList();
		return modifierList != null && modifierList.hasModifier(modifier);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByClass(CSharpIdentifier.class);
	}

	@Override
	@RequiredReadAction
	public String getName()
	{
		return CSharpPsiUtilImpl.getNameWithoutAt(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getNameWithAt()
	{
		return CSharpPsiUtilImpl.getNameWithAt(this);
	}

	@Override
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		return null;
	}
}
