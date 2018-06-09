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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.lexer.CSharpPreprocessorHightlightLexer;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpPreprocessorSyntaxHighlighter extends SyntaxHighlighterBase
{
	private static Map<IElementType, TextAttributesKey> ourKeys = new HashMap<>();

	static
	{
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_DEFINE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_UNDEF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_IF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_ELSE_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_ELIF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_REGION_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_ENDIF_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.MACRO_ENDREGION_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.PRAGMA_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.WARNING_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		safeMap(ourKeys, CSharpPreprocesorTokens.ERROR_KEYWORD, CSharpHighlightKey.MACRO_KEYWORD);
		//FIXME [VISTALL] highlight bug safeMap(ourKeys, CSharpPreprocesorTokens.LINE_COMMENT, CSharpHighlightKey.LINE_COMMENT);
	}

	@Nonnull
	@Override
	public Lexer getHighlightingLexer()
	{
		return new CSharpPreprocessorHightlightLexer();
	}

	@Nonnull
	@Override
	public TextAttributesKey[] getTokenHighlights(IElementType elementType)
	{
		return pack(ourKeys.get(elementType));
	}

}
