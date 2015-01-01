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
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 19.12.14
 */
public class CSharpPropertyUtil
{
	public static boolean isAutoProperty(@NotNull PsiElement element)
	{
		if(element instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration propertyDeclaration = (CSharpPropertyDeclaration) element;
			DotNetXXXAccessor[] accessors = propertyDeclaration.getAccessors();
			if(accessors.length == 2 && accessors[0].getCodeBlock() == null && accessors[1].getCodeBlock() == null)
			{
				return true;
			}

			// C# 6.0 specific readonly auto property
			if(accessors.length == 1 && accessors[0].getAccessorKind() == DotNetXXXAccessor.Kind.GET && accessors[0].getCodeBlock() == null &&
					propertyDeclaration.getInitializer() != null)
			{
				CSharpModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpModuleExtension.class);
				return extension != null && extension.getLanguageVersion().isAtLeast(CSharpLanguageVersion._6_0);
			}
		}
		return false;
	}
}
