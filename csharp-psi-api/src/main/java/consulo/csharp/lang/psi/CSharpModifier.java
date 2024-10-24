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

import consulo.dotnet.psi.DotNetModifier;
import consulo.util.collection.ArrayFactory;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
	OUT,
	ASYNC,
	IN,
	EXTERN,
	INTERFACE_ABSTRACT, // dummy modifier
	OPTIONAL; // dummy modifier

	public static final CSharpModifier[] EMPTY_ARRAY = new CSharpModifier[0];

	public static ArrayFactory<CSharpModifier> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpModifier[count];

	private static Map<DotNetModifier, CSharpModifier> ourReplaceMap = new HashMap<DotNetModifier, CSharpModifier>()
	{
		{
			put(DotNetModifier.STATIC, CSharpModifier.STATIC);
			put(DotNetModifier.PRIVATE, CSharpModifier.PRIVATE);
			put(DotNetModifier.PUBLIC, CSharpModifier.PUBLIC);
			put(DotNetModifier.PROTECTED, CSharpModifier.PROTECTED);
			put(DotNetModifier.INTERNAL, CSharpModifier.INTERNAL);
			put(DotNetModifier.ABSTRACT, CSharpModifier.ABSTRACT);
			put(DotNetModifier.SEALED, CSharpModifier.SEALED);
			put(DotNetModifier.COVARIANT, CSharpModifier.OUT);
			put(DotNetModifier.CONTRAVARIANT, CSharpModifier.IN);
		}
	};

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
	public String toString()
	{
		return getPresentableText();
	}

	@Override
	public String getPresentableText()
	{
		return name().toLowerCase(Locale.US);
	}

	@Nonnull
	public static CSharpModifier as(DotNetModifier modifierWithMask)
	{
		if(modifierWithMask instanceof CSharpModifier)
		{
			return (CSharpModifier) modifierWithMask;
		}
		else
		{
			CSharpModifier cSharpModifier = ourReplaceMap.get(modifierWithMask);
			if(cSharpModifier != null)
			{
				return cSharpModifier;
			}
			throw new IllegalArgumentException(modifierWithMask + " is cant be casted to CSharpModifier");
		}
	}
}
