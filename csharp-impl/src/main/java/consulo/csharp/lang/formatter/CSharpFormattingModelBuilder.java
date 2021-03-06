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

package consulo.csharp.lang.formatter;

import com.intellij.formatting.Block;
import com.intellij.formatting.DelegatingFormattingModelBuilder;
import com.intellij.formatting.FormattingModel;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.PsiBasedFormattingModel;
import consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.formatter.processors.CSharpSpacingSettings;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFormattingModelBuilder implements DelegatingFormattingModelBuilder
{
	@Override
	public boolean dontFormatMyModel()
	{
		return true;
	}

	@Nonnull
	@Override
	public FormattingModel createModel(PsiElement element, CodeStyleSettings settings)
	{
		PsiFile file = element.getContainingFile();

		FormattingDocumentModelImpl model = FormattingDocumentModelImpl.createOn(element.getContainingFile());

		CommonCodeStyleSettings commonSettings = settings.getCommonSettings(CSharpLanguage.INSTANCE);
		CSharpCodeStyleSettings customSettings = settings.getCustomSettings(CSharpCodeStyleSettings.class);

		CSharpSpacingSettings spacingSettings = new CSharpSpacingSettings(commonSettings, customSettings);
		
		Block rootBlock = new CSharpFormattingBlock(file.getNode(), null, null, settings, spacingSettings);

		return new PsiBasedFormattingModel(file, rootBlock, model);
	}

	@Nullable
	@Override
	public TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset)
	{
		return null;
	}
}
