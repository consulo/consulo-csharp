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

package org.mustbe.consulo.csharp.ide.highlight.check;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 11.03.14
 */
public abstract class AbstractCompilerCheck<T extends PsiElement> extends CompilerCheck<T>
{
	protected final String myId;

	public AbstractCompilerCheck()
	{
		myId = getClass().getSimpleName();
	}

	public boolean accept(@NotNull T element)
	{
		return false;
	}

	@Override
	public final CompilerCheckResult checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull T element)
	{
		if(accept(element))
		{
			CompilerCheckResult result = result(element.getTextRange());
			checkImpl(element, result);
			return result;
		}
		return null;
	}

	public void checkImpl(@NotNull T element, @NotNull CompilerCheckResult checkResult)
	{

	}
}
