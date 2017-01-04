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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 22.12.14
 */
public class CSharpMethodUtil
{
	public static enum Result
	{
		NO_GENERIC,
		CAN,
		CANT
	}

	public static boolean isDelegate(@Nullable PsiElement element)
	{
		return element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate();
	}

	@NotNull
	public static Result isCanInheritGeneric(@NotNull CSharpMethodDeclaration methodDeclaration)
	{
		int genericParametersCount = methodDeclaration.getGenericParametersCount();
		if(genericParametersCount == 0)
		{
			return Result.NO_GENERIC;
		}

		return methodDeclaration.getParameters().length != 0 ? Result.CANT : Result.CAN;
	}
}
