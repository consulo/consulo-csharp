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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.psi.impl.source.CSharpPreprocessorWarningImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;

/**
 * @author VISTALL
 * @since 2018-02-08
 */
public class CS1030 extends CompilerCheck<CSharpPreprocessorWarningImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpPreprocessorWarningImpl element)
	{
		IElementType elementType = PsiUtilCore.getElementType(element.getParent());
		if(elementType == CSharpPreprocessorElements.DISABLED_PREPROCESSOR_DIRECTIVE)
		{
			return null;
		}
		return newBuilder(element).setText(element.getText());
	}
}
