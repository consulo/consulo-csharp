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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 01.01.15
 */
public class CS0568 extends CompilerCheck<CSharpConstructorDeclaration>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpConstructorDeclaration element)
	{
		if(languageVersion.isAtLeast(CSharpLanguageVersion._6_0))
		{
			return null;
		}

		if(element.isDeConstructor())
		{
			return null;
		}

		PsiElement parent = element.getParent();
		if(parent instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) parent).isStruct() && element.getParameters().length == 0)
		{
			PsiElement nameIdentifier = element.getNameIdentifier();
			if(nameIdentifier == null)
			{
				return null;
			}
			return newBuilder(nameIdentifier);
		}
		return null;
	}
}
