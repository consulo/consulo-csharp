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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.msil.lang.psi.ModifierElementType;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilEntry;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class MsilToCSharpUtil
{
	@Nullable
	public static ModifierElementType toMsilModifier(DotNetModifier modifier)
	{
		if(modifier == DotNetModifier.STATIC)
		{
			return MsilTokens.STATIC_KEYWORD;
		}
		if(modifier instanceof CSharpModifier)
		{
			switch((CSharpModifier) modifier)
			{
				case PUBLIC:
					return MsilTokens.PUBLIC_KEYWORD;
				case PRIVATE:
					return MsilTokens.PRIVATE_KEYWORD;
				case PROTECTED:
					return MsilTokens.PROTECTED_KEYWORD;
				case STATIC:
					return MsilTokens.STATIC_KEYWORD;
				case SEALED:
					break;
				case READONLY:
					break;
				case UNSAFE:
					break;
				case PARAMS: //TODO [VISTALL] handle System.ParamArrayAttribute
					break;
				case THIS:  //TODO [VISTALL] handle System.Runtime.CompilerServices.ExtensionAttribute
					break;
				case ABSTRACT:
					return MsilTokens.ABSTRACT_KEYWORD;
				case PARTIAL:
					break;
			}
		}
		return null;
	}
	public static PsiElement wrap(PsiElement element)
	{
		if(element instanceof MsilEntry)
		{
			if(element instanceof MsilClassEntry)
			{
				return new MsilClassAsCSharpTypeDefinition((MsilClassEntry) element);
			}
		}
		return element;
	}
}
