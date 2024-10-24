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

package consulo.csharp.impl.lang.formatter;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.impl.lang.formatter.processors.CSharpSpacingSettings;
import consulo.language.Language;
import consulo.language.codeStyle.*;
import consulo.language.psi.PsiFile;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
@ExtensionImpl
public class CSharpFormattingModelBuilder implements DelegatingFormattingModelBuilder
{
	@Override
	public boolean dontFormatMyModel()
	{
		return true;
	}

	@Nonnull
	@Override
	public FormattingModel createModel(@Nonnull FormattingContext formattingContext)
	{
		CodeStyleSettings settings = formattingContext.getCodeStyleSettings();

		PsiFile file = formattingContext.getContainingFile();

		FormattingDocumentModel model = FormattingDocumentModel.create(file);

		CommonCodeStyleSettings commonSettings = settings.getCommonSettings(CSharpLanguage.INSTANCE);
		CSharpCodeStyleSettings customSettings = settings.getCustomSettings(CSharpCodeStyleSettings.class);

		CSharpSpacingSettings spacingSettings = new CSharpSpacingSettings(commonSettings, customSettings);

		Block rootBlock = new CSharpFormattingBlock(file.getNode(), null, null, settings, spacingSettings);

		return new PsiBasedFormattingModel(file, rootBlock, model);
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
