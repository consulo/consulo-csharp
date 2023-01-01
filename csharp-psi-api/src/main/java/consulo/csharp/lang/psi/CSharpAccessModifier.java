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

package consulo.csharp.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import java.util.Locale;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public enum CSharpAccessModifier
{
	NONE()
			{
				@Nonnull
				@Override
				public String getPresentableText()
				{
					return "";
				}
			},
	PUBLIC(CSharpModifier.PUBLIC),
	// protected internal need be first
	PROTECTED_INTERNAL(CSharpModifier.PROTECTED, CSharpModifier.INTERNAL),
	INTERNAL(CSharpModifier.INTERNAL),
	PROTECTED(CSharpModifier.PROTECTED),
	PRIVATE(CSharpModifier.PRIVATE);

	public static final CSharpAccessModifier[] VALUES = values();

	private final CSharpModifier[] myModifiers;

	CSharpAccessModifier(CSharpModifier... modifiers)
	{
		myModifiers = modifiers;
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpAccessModifier findModifier(@Nonnull DotNetModifierListOwner owner)
	{
		loop: for(CSharpAccessModifier value : VALUES)
		{
			if(value == NONE)
			{
				continue;
			}

			for(CSharpModifier modifier : value.myModifiers)
			{
				if(!owner.hasModifier(modifier))
				{
					continue loop;
				}
			}
			return value;
		}
		return NONE;
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpAccessModifier findModifierOrDefault(@Nonnull DotNetModifierListOwner owner)
	{
		final CSharpAccessModifier modifier = findModifier(owner);
		if(modifier == NONE)
		{
			if(owner instanceof CSharpTypeDeclaration)
			{
				return INTERNAL;
			}

			final PsiElement parent = owner.getParent();
			if(parent instanceof CSharpTypeDeclaration)
			{
				return PRIVATE;
			}
		}
		return modifier;
	}


	@Nonnull
	public CSharpModifier[] getModifiers()
	{
		return myModifiers;
	}

	@Nonnull
	public String getPresentableText()
	{
		return name().toLowerCase(Locale.US).replace("_", " ");
	}
}
