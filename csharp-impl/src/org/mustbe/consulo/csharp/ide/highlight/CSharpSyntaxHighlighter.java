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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.lexer.CSharpLexer;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;

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
		safeMap(ourKeys, CSharpTokens.LINE_DOC_COMMENT, CSharpHighlightKey.LINE_COMMENT);
		safeMap(ourKeys, CSharpTokens.BLOCK_COMMENT, CSharpHighlightKey.BLOCK_COMMENT);
		safeMap(ourKeys, CSharpTokenSets.STRINGS, CSharpHighlightKey.STRING);
		safeMap(ourKeys, CSharpTokenSets.KEYWORDS, CSharpHighlightKey.KEYWORD);
		safeMap(ourKeys, CSharpTokens.INTEGER_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.LONG_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.NON_ACTIVE_SYMBOL, CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES);
		safeMap(ourKeys, CSharpTokens.FLOAT_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.DOUBLE_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.DECIMAL_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.ULONG_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, CSharpTokens.UINTEGER_LITERAL, CSharpHighlightKey.NUMBER);
		safeMap(ourKeys, StringEscapesTokenTypes.VALID_STRING_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE);
		safeMap(ourKeys, StringEscapesTokenTypes.INVALID_CHARACTER_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
		safeMap(ourKeys, StringEscapesTokenTypes.INVALID_UNICODE_ESCAPE_TOKEN, DefaultLanguageHighlighterColors.INVALID_STRING_ESCAPE);
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer()
	{
		return new CSharpLexer();
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType elementType)
	{
		return pack(ourKeys.get(elementType));
	}
}
