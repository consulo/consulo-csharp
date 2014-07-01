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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 01.07.14
 */
public class MethodGenerateUtil
{
	@Nullable
	public static String getDefaultValueForType(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		if(typeRef.isNullable())
		{
			return "null";
		}

		if(typeRef == CSharpNativeTypeRef.INT)
		{
			return "0";
		}
		else if(typeRef == CSharpNativeTypeRef.LONG)
		{
			return "0l";
		}
		else if(typeRef == CSharpNativeTypeRef.UINT)
		{
			return "0u";
		}
		else if(typeRef == CSharpNativeTypeRef.ULONG)
		{
			return "0ul";
		}
		else if(typeRef == CSharpNativeTypeRef.BOOL)
		{
			return "false";
		}

		PsiElement resolve = typeRef.resolve(scope);
		if(resolve instanceof DotNetGenericParameter)
		{
			return "default(" + ((DotNetGenericParameter) resolve).getName() + ")";
		}

		return null;
	}
}
