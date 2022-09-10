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

package consulo.csharp.ide.highlight;

import consulo.colorScheme.EditorColorsScheme;
import consulo.colorScheme.TextAttributesKey;
import consulo.csharp.cfs.impl.CSharpCfsLanguageVersion;
import consulo.csharp.lang.doc.impl.ide.highlight.CSharpDocSyntaxHighlighter;
import consulo.csharp.lang.impl.psi.CSharpTemplateTokens;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.cfs.ide.highlight.CfsSyntaxHighlighter;
import consulo.dotnet.cfs.lang.CfsTokens;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.LayerDescriptor;
import consulo.language.editor.highlight.LayeredLexerEditorHighlighter;
import consulo.language.lexer.Lexer;
import consulo.language.lexer.StringLiteralLexer;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpEditorHighlighter extends LayeredLexerEditorHighlighter
{
	public CSharpEditorHighlighter(@Nullable final VirtualFile virtualFile, @Nonnull final EditorColorsScheme colors)
	{
		super(new CSharpSyntaxHighlighter(), colors);

		registerLayer(CSharpCfsLanguageVersion.getInstance().getExpressionElementType(), new LayerDescriptor(new CSharpSyntaxHighlighter(), ""));

		registerLayer(CSharpTokens.STRING_LITERAL, new LayerDescriptor(new CSharpSyntaxHighlighter()
		{
			@Nonnull
			@Override
			public Lexer getHighlightingLexer()
			{
				return new StringLiteralLexer('\"', CSharpTokens.STRING_LITERAL);
			}
		}, ""));
		registerLayer(CSharpTokensImpl.LINE_DOC_COMMENT, new LayerDescriptor(new CSharpDocSyntaxHighlighter(), ""));
		registerLayer(CSharpTokens.CHARACTER_LITERAL, new LayerDescriptor(new CSharpSyntaxHighlighter()
		{
			@Nonnull
			@Override
			public Lexer getHighlightingLexer()
			{
				return new StringLiteralLexer('\'', CSharpTokens.CHARACTER_LITERAL);
			}
		}, ""));
		registerLayer(CSharpTokensImpl.INTERPOLATION_STRING_LITERAL, new LayerDescriptor(new CfsSyntaxHighlighter(CSharpCfsLanguageVersion.getInstance())
		{
			@Nonnull
			@Override
			public TextAttributesKey[] getTokenHighlights(IElementType elementType)
			{
				if(elementType == CfsTokens.TEXT)
				{
					return pack(CSharpHighlightKey.STRING);
				}
				return super.getTokenHighlights(elementType);
			}
		}, ""));
		registerLayer(CSharpTemplateTokens.PREPROCESSOR_FRAGMENT, new LayerDescriptor(new CSharpPreprocessorSyntaxHighlighter(), ""));
	}
}
