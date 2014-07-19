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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class CSharpMethodImplUtil
{
	public static boolean isExtensionMethod(@NotNull PsiElement element)
	{
		if(element instanceof CSharpMethodDeclaration)
		{
			DotNetParameter[] parameters = ((CSharpMethodDeclaration) element).getParameters();
			return parameters.length > 0 && parameters[0].hasModifier(CSharpModifier.THIS);
		}
		return false;
	}

	public static boolean isExtensionWrapper(@Nullable PsiElement element)
	{
		return element instanceof CSharpMethodDeclaration && element.getUserData(CSharpResolveUtil.EXTENSION_METHOD_WRAPPER) == Boolean.TRUE;
	}
}
