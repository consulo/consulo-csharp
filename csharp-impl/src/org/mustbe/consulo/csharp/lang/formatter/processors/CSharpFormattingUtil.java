/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.formatter.processors;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpXXXAccessorOwner;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 28.05.2015
 */
public class CSharpFormattingUtil
{
	public static boolean isAutoAccessorOwner(@NotNull PsiElement element)
	{
		if(element instanceof CSharpXXXAccessorOwner)
		{
			DotNetXXXAccessor[] accessors = ((CSharpXXXAccessorOwner) element).getAccessors();
			for(DotNetXXXAccessor accessor : accessors)
			{
				PsiElement codeBlock = accessor.getCodeBlock();
				if(codeBlock != null)
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
}
