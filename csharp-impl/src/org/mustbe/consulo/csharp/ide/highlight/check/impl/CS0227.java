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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.AbstractCompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpModifierListImpl;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0227 extends AbstractCompilerCheck<CSharpModifierListImpl>
{
	@Override
	public boolean accept(@NotNull CSharpModifierListImpl list)
	{
		PsiElement modifier = list.getModifier(CSharpTokens.UNSAFE_KEYWORD);
		if(modifier == null)
		{
			return false;
		}

		BaseCSharpModuleExtension extension = ModuleUtilCore.getExtension(list, BaseCSharpModuleExtension.class);

		return extension != null && !extension.isAllowUnsafeCode();
	}

	@Override
	public void checkImpl(
			@NotNull CSharpModifierListImpl list, @NotNull CompilerCheckResult checkResult)
	{
		PsiElement modifier = list.getModifier(CSharpTokens.UNSAFE_KEYWORD);
		assert modifier != null;
		checkResult.setTextRange(modifier.getTextRange());
	}
}
