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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 03.09.14
 */
public class CSharpPresentationUtil
{
	public static Map<String, String> ourNullableCache = new HashMap<String, String>()
	{
		{
			put(DotNetTypes.System.Object, "object");
			put(DotNetTypes.System.String, "string");
		}
	};

	public static Map<String, String> ourNotNullCache = new HashMap<String, String>()
	{
		{
			put(DotNetTypes.System.SByte, "sbyte");
			put(DotNetTypes.System.Byte, "byte");
			put(DotNetTypes.System.Int16, "short");
			put(DotNetTypes.System.UInt16, "ushort");
			put(DotNetTypes.System.Int32, "int");
			put(DotNetTypes.System.UInt32, "uint");
			put(DotNetTypes.System.Int64, "long");
			put(DotNetTypes.System.UInt64, "ulong");
			put(DotNetTypes.System.Single, "float");
			put(DotNetTypes.System.Double, "double");
			put(DotNetTypes.System.Char, "char");
			put(DotNetTypes.System.Void, "void");
			put(DotNetTypes.System.Boolean, "bool");
			put(DotNetTypes.System.Decimal, "decimal");
		}
	};

	@NotNull
	public static String getPresentableText(@NotNull DotNetTypeRef typeRef)
	{
		return getPresentableText(typeRef.getQualifiedText(), typeRef.isNullable());
	}

	@NotNull
	public static String getPresentableText(@NotNull String qualifiedText, boolean nullable)
	{
		String shortText = ourNullableCache.get(qualifiedText);
		if(shortText != null)
		{
			return shortText;
		}

		shortText = ourNotNullCache.get(qualifiedText);
		if(shortText != null)
		{
			if(nullable)
			{
				return shortText + "?";
			}
			else
			{
				return shortText;
			}
		}
		else
		{
			return qualifiedText;
		}
	}
}
