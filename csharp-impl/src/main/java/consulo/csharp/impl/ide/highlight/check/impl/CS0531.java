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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetCodeBlockOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNameIdentifierOwner;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19.12.14
 */
public class CS0531 extends CompilerCheck<DotNetCodeBlockOwner>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetCodeBlockOwner element)
	{
		PsiElement superParent = null;

		if(element instanceof DotNetXAccessor)
		{
			superParent = element.getParent().getParent();
		}
		else
		{
			superParent = element.getParent();
		}

		if(((CSharpCodeBodyProxy) element.getCodeBlock()).isNotSemicolonAndNotEmpty() && superParent instanceof DotNetTypeDeclaration && ((DotNetTypeDeclaration) superParent).isInterface())
		{
			PsiElement nameIdentifier = ((PsiNameIdentifierOwner) element).getNameIdentifier();
			if(nameIdentifier == null)
			{
				return null;
			}
			return newBuilder(nameIdentifier, formatElement(element)).withQuickFix(new CS0500.RemoveCodeBlockFix(element));
		}
		return null;
	}
}
