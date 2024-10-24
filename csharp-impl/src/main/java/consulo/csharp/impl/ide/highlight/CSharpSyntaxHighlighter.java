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

package consulo.csharp.impl.ide.highlight;

import consulo.codeEditor.CodeInsightColors;
import consulo.codeEditor.DefaultLanguageHighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.cfs.lang.CfsTokens;
import consulo.language.ast.IElementType;
import consulo.language.ast.StringEscapesTokenTypes;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import jakarta.annotation.Nonnull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpSyntaxHighlighter extends SyntaxHighlighterBase
{
	private static Map<IElementType, TextAttributesKey> ourKeys = new HashMap<IElementType, TextAttributesKey>();

	static
	{
		safeMap(ourKeys, CSharpTokens.LINE_COMMENT, CSharpHighlightKey.LINE_COMMENT);
		safeMap(ourKeys, CSharpTokens.BLOCK_COMMENT, CSharpHighlightKey.BLOCK_COMMENT);
		safeMap(ourKeys, CSharpTokenSets.STRINGS, CSharpHighlightKey.STRING);
		safeMap(ourKeys, CSharpTokenSets.KEYWORDS, CSharpHighlightKey.KEYWORD);
		safeMap(ourKeys, CSharpTokens.INTEGER_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.LONG_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.FLOAT_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.DOUBLE_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.DECIMAL_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.ULONG_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.UINTEGER_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.COMMA, CSharpHighlightKey.COMMA);
		safeMap(ourKeys, CSharpTokens.COLON, CSharpHighlightKey.COLON);
		safeMap(ourKeys, CSharpTokens.COLONCOLON, CSharpHighlightKey.COLON);
		safeMap(ourKeys, CSharpTokens.SEMICOLON, CSharpHighlightKey.SEMICOLON);
		safeMap(ourKeys, CSharpTokens.LBRACE, CSharpHighlightKey.BRACES);
		safeMap(ourKeys, CSharpTokens.RBRACE, CSharpHighlightKey.BRACES);
		safeMap(ourKeys, CSharpTokens.LPAR, CSharpHighlightKey.PARENTHESES);
		safeMap(ourKeys, CSharpTokens.RPAR, CSharpHighlightKey.PARENTHESES);
		safeMap(ourKeys, CSharpTokens.LBRACKET, CSharpHighlightKey.BRACKETS);
		safeMap(ourKeys, CSharpTokens.RBRACKET, CSharpHighlightKey.BRACKETS);
		safeMap(ourKeys, CSharpTokens.DOT, CSharpHighlightKey.DOT);
		safeMap(ourKeys, CSharpTokens.DARROW, CSharpHighlightKey.ARROW);
		safeMap(ourKeys, CSharpTokens.ARROW, CSharpHighlightKey.ARROW);
		safeMap(ourKeys, CSharpTokenSets.OVERLOADING_OPERATORS, CSharpHighlightKey.OPERATION_SIGN);
		safeMap(ourKeys, CSharpTokenSets.ASSIGNMENT_OPERATORS, CSharpHighlightKey.OPERATION_SIGN);
		safeMap(ourKeys, CSharpTokens.OROR, CSharpHighlightKey.OPERATION_SIGN);
		safeMap(ourKeys, CSharpTokens.ANDAND, CSharpHighlightKey.OPERATION_SIGN);
		safeMap(ourKeys, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
		safeMap(ourKeys, StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
		safeMap(ourKeys, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);

		safeMap(ourKeys, CSharpPreprocessorElements.DISABLED_PREPROCESSOR_DIRECTIVE, CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
		safeMap(ourKeys, CSharpTokens.NON_ACTIVE_SYMBOL, CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);

		// from CfsSyntaxHighlighter
		safeMap(ourKeys, CfsTokens.FORMAT, DefaultLanguageHighlighterColors.INSTANCE_FIELD);
		safeMap(ourKeys, CfsTokens.TEXT, CSharpHighlightKey.STRING);
	}

	@Nonnull
	@Override
	public Lexer getHighlightingLexer()
	{
		return new CSharpHighlighterLexer();
	}

	@Nonnull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType elementType)
	{
		return pack(ourKeys.get(elementType));
	}
}
