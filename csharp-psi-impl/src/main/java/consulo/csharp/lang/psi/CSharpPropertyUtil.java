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

import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.psi.DotNetCodeBodyProxy;
import consulo.dotnet.psi.DotNetXAccessor;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 19.12.14
 */
public class CSharpPropertyUtil
{
	@RequiredReadAction
	public static boolean isAutoProperty(@Nonnull PsiElement element)
	{
		if(element instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration propertyDeclaration = (CSharpPropertyDeclaration) element;
			DotNetXAccessor[] accessors = propertyDeclaration.getAccessors();
			if(accessors.length == 2 && isSemicolonOrEmpty(accessors[0].getCodeBlock()) && isSemicolonOrEmpty(accessors[1].getCodeBlock()))
			{
				return true;
			}

			// C# 6.0 specific readonly auto property
			if(accessors.length == 1 && accessors[0].getAccessorKind() == DotNetXAccessor.Kind.GET && isSemicolonOrEmpty(accessors[0].getCodeBlock()))
			{
				return CSharpModuleUtil.findLanguageVersion(element).isAtLeast(CSharpLanguageVersion._6_0);
			}
		}
		return false;
	}

	@RequiredReadAction
	private static boolean isSemicolonOrEmpty(DotNetCodeBodyProxy proxy)
	{
		CSharpCodeBodyProxy codeBlock = (CSharpCodeBodyProxy) proxy;
		return codeBlock.isSemicolonOrEmpty();
	}
}
