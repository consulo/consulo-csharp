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

package org.mustbe.consulo.csharp.lang.psi;

import java.util.Locale;

import org.consulo.annotations.Immutable;
import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public enum CSharpAccessModifier
{
	NONE()
			{
				@NotNull
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

	@Immutable
	public static final CSharpAccessModifier[] VALUES = values();

	private final CSharpModifier[] myModifiers;

	CSharpAccessModifier(CSharpModifier... modifiers)
	{
		myModifiers = modifiers;
	}

	@NotNull
	@RequiredReadAction
	public static CSharpAccessModifier findModifier(@NotNull DotNetModifierListOwner owner)
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

	@NotNull
	@Immutable
	public CSharpModifier[] getModifiers()
	{
		return myModifiers;
	}

	@NotNull
	@LazyInstance
	public String getPresentableText()
	{
		return name().toLowerCase(Locale.US).replace("_", " ");
	}
}
