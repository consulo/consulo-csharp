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
import org.mustbe.consulo.csharp.lang.lexer.CSharpPreprocessorLexer;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpMacroSyntaxHighlighter extends SyntaxHighlighterBase
{
	private static Map<IElementType, TextAttributesKey> ourKeys = new HashMap<IElementType, TextAttributesKey>();

	static
	{
		//safeMap(ourKeys, CSharpMacroTokens.BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);
		safeMap(ourKeys, CSharpMacroTokens.SHARP, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.DEFINE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.UNDEF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.IF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.ELSE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.ELIF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.REGION_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.ENDIF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.ENDREGION_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpMacroTokens.COMMENT, CSharpHighlightKey.LINE_COMMENT);
	}

	@NotNull
	@Override
	public Lexer getHighlightingLexer()
	{
		return new CSharpPreprocessorLexer();
	}

	@NotNull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType elementType)
	{
		return pack(ourKeys.get(elementType));
	}

}
