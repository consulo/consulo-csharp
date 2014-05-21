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

package org.mustbe.consulo.csharp.ide.highlight;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.StringLiteralLexer;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.ex.util.LayerDescriptor;
import com.intellij.openapi.editor.ex.util.LayeredLexerEditorHighlighter;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpEditorHighlighter extends LayeredLexerEditorHighlighter
{
	public CSharpEditorHighlighter(@Nullable final VirtualFile virtualFile, @NotNull final EditorColorsScheme colors)
	{
		super(new CSharpSyntaxHighlighter(), colors);
		registerLayer(CSharpTemplateTokens.MACRO_FRAGMENT, new LayerDescriptor(new CSharpMacroSyntaxHighlighter(), ""));
		registerLayer(CSharpTokens.STRING_LITERAL, new LayerDescriptor(new CSharpSyntaxHighlighter()
		{
			@NotNull
			@Override
			public Lexer getHighlightingLexer()
			{
				return new StringLiteralLexer('\"', CSharpTokens.STRING_LITERAL);
			}
		}, ""));
		registerLayer(CSharpTokens.CHARACTER_LITERAL, new LayerDescriptor(new CSharpSyntaxHighlighter()
		{
			@NotNull
			@Override
			public Lexer getHighlightingLexer()
			{
				return new StringLiteralLexer('\'', CSharpTokens.CHARACTER_LITERAL);
			}
		}, ""));

	}

}
