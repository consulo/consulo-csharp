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

package consulo.csharp.lang.doc.impl.ide.highlight;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import consulo.csharp.lang.doc.impl.lexer.CSharpDocLexer;
import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import consulo.codeEditor.HighlighterColors;
import consulo.colorScheme.TextAttributesKey;
import consulo.language.ast.IElementType;

/**
 * @author VISTALL
 * @since 01.03.2015
 */
public class CSharpDocSyntaxHighlighter extends SyntaxHighlighterBase
{
	private static final Map<IElementType, TextAttributesKey> keys1;
	private static final Map<IElementType, TextAttributesKey> keys2;

	static
	{
		keys1 = new HashMap<IElementType, TextAttributesKey>();
		keys2 = new HashMap<IElementType, TextAttributesKey>();

		keys1.put(CSharpDocTokenType.XML_DATA_CHARACTERS, CSharpDocHighlightKey.DOC_COMMENT);

		keys1.put(CSharpDocTokenType.XML_COMMENT_START, CSharpDocHighlightKey.DOC_COMMENT);
		keys1.put(CSharpDocTokenType.XML_COMMENT_END, CSharpDocHighlightKey.DOC_COMMENT);
		keys1.put(CSharpDocTokenType.XML_COMMENT_CHARACTERS, CSharpDocHighlightKey.DOC_COMMENT);


		keys1.put(CSharpDocTokenType.XML_START_TAG_START, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_END_TAG_START, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_TAG_END, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_EMPTY_ELEMENT_END, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_TAG_NAME, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.TAG_WHITE_SPACE, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_NAME, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_TAG_CHARACTERS, CSharpDocHighlightKey.DOC_COMMENT_TAG);
		keys1.put(CSharpDocTokenType.XML_BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);

		keys1.put(CSharpDocTokenType.XML_EQ, CSharpDocHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(CSharpDocTokenType.XML_ATTRIBUTE_VALUE_TOKEN, CSharpDocHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, CSharpDocHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(CSharpDocTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, CSharpDocHighlightKey.DOC_COMMENT_ATTRIBUTE);
	}

	@Override
	@Nonnull
	public Lexer getHighlightingLexer()
	{
		return new CSharpDocLexer();
	}

	@Override
	@Nonnull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(keys1.get(tokenType), keys2.get(tokenType));
	}
}
