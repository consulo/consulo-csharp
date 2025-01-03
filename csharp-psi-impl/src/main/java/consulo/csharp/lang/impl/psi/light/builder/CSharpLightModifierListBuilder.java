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

package consulo.csharp.lang.impl.psi.light.builder;

import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.language.psi.PsiElement;
import consulo.language.impl.psi.LightElement;
import consulo.language.Language;
import consulo.language.psi.PsiManager;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class CSharpLightModifierListBuilder extends LightElement implements DotNetModifierList
{
	private final List<CSharpModifier> myModifiers;

	public CSharpLightModifierListBuilder(List<CSharpModifier> modifiers, PsiManager manager, Language language)
	{
		super(manager, language);
		myModifiers = modifiers;
	}

	@Override
	public void addModifier(@Nonnull DotNetModifier modifier)
	{
		myModifiers.add(CSharpModifier.as(modifier));
	}

	@Override
	public void removeModifier(@Nonnull DotNetModifier modifier)
	{
		myModifiers.remove(CSharpModifier.as(modifier));
	}

	@Nonnull
	@Override
	public DotNetModifier[] getModifiers()
	{
		return myModifiers.toArray(new DotNetModifier[myModifiers.size()]);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		return DotNetAttribute.EMPTY_ARRAY;
	}

	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

	@Override
	public boolean hasModifierInTree(@Nonnull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

	@Nullable
	@Override
	public PsiElement getModifierElement(DotNetModifier modifier)
	{
		return null;
	}

	@Nonnull
	@Override
	public List<PsiElement> getModifierElements(@Nonnull DotNetModifier modifier)
	{
		return Collections.emptyList();
	}

	@Override
	public String toString()
	{
		return "CSharpLightModifierListBuilder: " + myModifiers;
	}
}
