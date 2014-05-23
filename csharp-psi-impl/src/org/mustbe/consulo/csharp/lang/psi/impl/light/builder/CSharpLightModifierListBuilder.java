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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import com.intellij.lang.Language;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class CSharpLightModifierListBuilder extends LightElement implements DotNetModifierList
{
	private final List<DotNetModifier> myModifiers;

	public CSharpLightModifierListBuilder(List<DotNetModifier> modifiers, PsiManager manager, Language language)
	{
		super(manager, language);
		myModifiers = modifiers;
	}

	@NotNull
	@Override
	public DotNetModifier[] getModifiers()
	{
		return myModifiers.toArray(new DotNetModifier[myModifiers.size()]);
	}

	@NotNull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		return new DotNetAttribute[0];
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

	@Override
	public boolean hasModifierInTree(@NotNull DotNetModifier modifier)
	{
		return myModifiers.contains(CSharpModifier.as(modifier));
	}

	@Nullable
	@Override
	public PsiElement getModifier(IElementType elementType)
	{
		return null;
	}

	@Override
	public String toString()
	{
		return null;
	}
}
