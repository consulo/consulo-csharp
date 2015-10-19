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

package org.mustbe.consulo.csharp.lang.formatter;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.formatter.processors.CSharpIndentProcessor;
import org.mustbe.consulo.csharp.lang.formatter.processors.CSharpSpacingProcessor;
import org.mustbe.consulo.csharp.lang.formatter.processors.CSharpWrappingProcessor;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.formatting.ASTBlock;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.Wrap;
import com.intellij.formatting.templateLanguages.DataLanguageBlockWrapper;
import com.intellij.formatting.templateLanguages.TemplateLanguageBlock;
import com.intellij.lang.ASTNode;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public class CSharpFormattingBlock extends TemplateLanguageBlock implements CSharpElements, CSharpTokens, CSharpTokenSets
{
	private final CSharpWrappingProcessor myWrappingProcessor;
	private final CSharpIndentProcessor myIndentProcessor;
	private final CSharpSpacingProcessor mySpacingProcessor;

	public CSharpFormattingBlock(
			@NotNull CSharpFormattingModelBuilder blockFactory,
			@NotNull CodeStyleSettings settings,
			@NotNull ASTNode node,
			@Nullable List<DataLanguageBlockWrapper> foreignChildren)
	{
		super(blockFactory, settings, node, foreignChildren);
		CommonCodeStyleSettings commonSettings = settings.getCommonSettings(CSharpLanguage.INSTANCE);
		CSharpCodeStyleSettings customSettings = settings.getCustomSettings(CSharpCodeStyleSettings.class);

		myWrappingProcessor = new CSharpWrappingProcessor(node, commonSettings, customSettings);
		myIndentProcessor = new CSharpIndentProcessor(node, commonSettings, customSettings);
		mySpacingProcessor = new CSharpSpacingProcessor(this, commonSettings, customSettings);
	}

	@Nullable
	@Override
	public Spacing getSpacing(@Nullable Block child1, @NotNull Block child2)
	{
		return mySpacingProcessor.getSpacing((ASTBlock)child1, (ASTBlock)child2);
	}

	@Nullable
	@Override
	public Wrap getWrap()
	{
		return myWrappingProcessor.getWrap();
	}

	@Override
	public Indent getIndent()
	{
		return myIndentProcessor.getIndent();
	}

	@Nullable
	@Override
	protected Indent getChildIndent()
	{
		return myIndentProcessor.getChildIndent();
	}

	@Override
	protected IElementType getTemplateTextElementType()
	{
		return CSharpTemplateTokens.PREPROCESSOR_DIRECTIVE;
	}
}
