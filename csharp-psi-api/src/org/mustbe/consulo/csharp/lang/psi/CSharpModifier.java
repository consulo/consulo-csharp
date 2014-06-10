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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public enum CSharpModifier implements DotNetModifier
{
	PUBLIC,
	PRIVATE,
	PROTECTED,
	STATIC,
	SEALED,
	READONLY,
	UNSAFE,
	PARAMS,
	THIS,
	ABSTRACT,
	PARTIAL,
	INTERNAL,
	REF,
	NEW,
	OVERRIDE,
	VIRTUAL,
	OUT;

	public static CSharpModifier[] EMPTY_ARRAY = new CSharpModifier[0];

	private int myMask;

	CSharpModifier()
	{
		myMask = 1 << ordinal();
	}

	public int mask()
	{
		return myMask;
	}

	@Override
	public String getPresentableText()
	{
		return name().toLowerCase();
	}

	@NotNull
	public static CSharpModifier as(DotNetModifier modifierWithMask)
	{
		if(modifierWithMask == DotNetModifier.STATIC)
		{
			return CSharpModifier.STATIC;
		}
		else if(modifierWithMask == DotNetModifier.SEALED)
		{
			return CSharpModifier.SEALED;
		}

		if(modifierWithMask instanceof CSharpModifier)
		{
			return (CSharpModifier) modifierWithMask;
		}
		else
		{
			throw new IllegalArgumentException(modifierWithMask + " is cant be casted to CSharpModifier");
		}
	}
}
