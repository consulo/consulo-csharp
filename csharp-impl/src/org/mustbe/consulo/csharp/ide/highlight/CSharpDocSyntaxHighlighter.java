/*
 * Copyright 2013-2015 must-be.org
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
import com.intellij.lexer.Lexer;
import com.intellij.lexer.XmlHighlightingLexer;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.XmlHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;

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

		keys1.put(XmlTokenType.XML_DATA_CHARACTERS, CSharpHighlightKey.DOC_COMMENT);

		keys1.put(XmlTokenType.XML_COMMENT_START, CSharpHighlightKey.DOC_COMMENT);
		keys1.put(XmlTokenType.XML_COMMENT_END, CSharpHighlightKey.DOC_COMMENT);
		keys1.put(XmlTokenType.XML_COMMENT_CHARACTERS, CSharpHighlightKey.DOC_COMMENT);
		keys1.put(XmlTokenType.XML_CONDITIONAL_COMMENT_END, CSharpHighlightKey.DOC_COMMENT);
		keys1.put(XmlTokenType.XML_CONDITIONAL_COMMENT_END_START, CSharpHighlightKey.DOC_COMMENT);
		keys1.put(XmlTokenType.XML_CONDITIONAL_COMMENT_START, CSharpHighlightKey.DOC_COMMENT);
		keys1.put(XmlTokenType.XML_CONDITIONAL_COMMENT_START_END, CSharpHighlightKey.DOC_COMMENT);

		keys1.put(XmlTokenType.XML_START_TAG_START, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_END_TAG_START, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_TAG_END, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_EMPTY_ELEMENT_END, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_TAG_NAME, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.TAG_WHITE_SPACE, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_NAME, CSharpHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(XmlTokenType.XML_CONDITIONAL_IGNORE,  CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_CONDITIONAL_INCLUDE, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys1.put(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, CSharpHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, CSharpHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, CSharpHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(XmlTokenType.XML_EQ, CSharpHighlightKey.DOC_COMMENT_ATTRIBUTE);
		keys1.put(XmlTokenType.XML_TAG_CHARACTERS, CSharpHighlightKey.DOC_COMMENT_TAG);

		/*keys2.put(XmlTokenType.XML_TAG_NAME, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys2.put(XmlTokenType.XML_CONDITIONAL_INCLUDE, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys2.put(XmlTokenType.XML_CONDITIONAL_INCLUDE, CSharpHighlightKey.DOC_COMMENT_TAG);
		keys2.put(XmlTokenType.XML_NAME, XmlHighlighterColors.XML_ATTRIBUTE_NAME);
		keys2.put(XmlTokenType.XML_ATTRIBUTE_VALUE_TOKEN, XmlHighlighterColors.XML_ATTRIBUTE_VALUE);
		keys2.put(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, XmlHighlighterColors.XML_ATTRIBUTE_VALUE);
		keys2.put(XmlTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER, XmlHighlighterColors.XML_ATTRIBUTE_VALUE);
		keys2.put(XmlTokenType.XML_EQ, XmlHighlighterColors.XML_ATTRIBUTE_NAME);
		keys2.put(XmlTokenType.XML_TAG_CHARACTERS, XmlHighlighterColors.XML_ATTRIBUTE_VALUE);*/

		keys1.put(XmlTokenType.XML_BAD_CHARACTER, HighlighterColors.BAD_CHARACTER);

		keys1.put(XmlTokenType.XML_DECL_START, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_DECL_START, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_CONDITIONAL_SECTION_START, XmlHighlighterColors.XML_PROLOGUE);
		keys2.put(XmlTokenType.XML_CONDITIONAL_SECTION_START, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_CONDITIONAL_SECTION_END, XmlHighlighterColors.XML_PROLOGUE);
		keys2.put(XmlTokenType.XML_CONDITIONAL_SECTION_END, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_DECL_END, XmlHighlighterColors.XML_PROLOGUE);
		keys2.put(XmlTokenType.XML_DECL_END, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_PI_START, XmlHighlighterColors.XML_PROLOGUE);
		keys1.put(XmlTokenType.XML_PI_END, XmlHighlighterColors.XML_PROLOGUE);
		keys1.put(XmlTokenType.XML_DOCTYPE_END, XmlHighlighterColors.XML_PROLOGUE);
		keys2.put(XmlTokenType.XML_DOCTYPE_END, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_DOCTYPE_START, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_DOCTYPE_START, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_DOCTYPE_SYSTEM, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_DOCTYPE_SYSTEM, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_DOCTYPE_PUBLIC, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_DOCTYPE_PUBLIC, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_DOCTYPE_PUBLIC, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_DOCTYPE_PUBLIC, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_ATTLIST_DECL_START, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_ATTLIST_DECL_START, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_ELEMENT_DECL_START, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_ELEMENT_DECL_START, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys1.put(XmlTokenType.XML_ENTITY_DECL_START, XmlHighlighterColors.XML_TAG);
		keys2.put(XmlTokenType.XML_ENTITY_DECL_START, CSharpHighlightKey.DOC_COMMENT_TAG);

		keys2.put(XmlTokenType.XML_CHAR_ENTITY_REF, XmlHighlighterColors.XML_ENTITY_REFERENCE);
		keys2.put(XmlTokenType.XML_ENTITY_REF_TOKEN, XmlHighlighterColors.XML_ENTITY_REFERENCE);
	}

	@NotNull
	public Lexer getHighlightingLexer()
	{
		return new XmlHighlightingLexer();
	}

	@NotNull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(keys1.get(tokenType), keys2.get(tokenType));
	}
}
