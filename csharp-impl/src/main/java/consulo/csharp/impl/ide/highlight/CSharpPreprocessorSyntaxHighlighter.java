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

import consulo.colorScheme.TextAttributesKey;
import consulo.csharp.editor.highlight.CSharpHighlightKey;
import consulo.csharp.lang.impl.lexer.CSharpPreprocessorHightlightLexer;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorTokens;
import consulo.language.ast.IElementType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpPreprocessorSyntaxHighlighter extends SyntaxHighlighterBase
{
	private static Map<IElementType, TextAttributesKey> ourKeys = new HashMap<>();

	static
	{
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_DEFINE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_UNDEF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_IF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_ELSE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_ELIF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_REGION_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_ENDIF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.MACRO_ENDREGION_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.PRAGMA_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.WARNING_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.NULLABLE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocessorTokens.ERROR_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		//FIXME [VISTALL] highlight bug safeMap(ourKeys, CSharpPreprocessorTokens.LINE_COMMENT, CSharpHighlightKey.LINE_COMMENT);
	}

	@Override
	public Lexer getHighlightingLexer()
	{
		return new CSharpPreprocessorHightlightLexer();
	}

	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType elementType)
	{
		return pack(ourKeys.get(elementType));
	}

}
