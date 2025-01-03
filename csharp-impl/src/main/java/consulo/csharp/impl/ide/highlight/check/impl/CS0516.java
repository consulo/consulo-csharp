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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpConstructorSuperCallImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CS0516 extends CompilerCheck<CSharpConstructorSuperCallImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpConstructorSuperCallImpl element)
	{
		PsiElement psiElement = element.resolveToCallable();
		PsiElement parent = element.getParent();
		return psiElement != null && psiElement == parent ? newBuilder(element, formatElement(parent)) : null;
	}
}
