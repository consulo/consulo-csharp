/*
 * Copyright 2013-2018 consulo.io
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
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.source.CSharpPreprocessorErrorImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiUtilCore;
import consulo.localize.LocalizeValue;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2018-06-09
 */
public class CS1029 extends CompilerCheck<CSharpPreprocessorErrorImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpPreprocessorErrorImpl element)
	{
		IElementType elementType = PsiUtilCore.getElementType(element.getParent());
		if(elementType == CSharpPreprocessorElements.DISABLED_PREPROCESSOR_DIRECTIVE)
		{
			return null;
		}
		return newBuilder(element).withText(LocalizeValue.of(element.getText()));
	}
}

