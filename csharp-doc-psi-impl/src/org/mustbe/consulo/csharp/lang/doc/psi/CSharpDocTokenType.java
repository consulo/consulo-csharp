/*
 * Copyright 2013-2015 must-be.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License", CSharpDocLanguage.INSTANCE);
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

package org.mustbe.consulo.csharp.lang.doc.psi;

import org.mustbe.consulo.csharp.lang.doc.CSharpDocLanguage;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 03.03.2015
 *
 * Base code is {@see com.intellij.psi.xml.XmlTokenType}
 */
public interface CSharpDocTokenType
{
	IElementType XML_START_TAG_START = new IElementType("XML_START_TAG_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_END_TAG_START = new IElementType("XML_END_TAG_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_TAG_END = new IElementType("XML_TAG_END", CSharpDocLanguage.INSTANCE);
	IElementType XML_EMPTY_ELEMENT_END = new IElementType("XML_EMPTY_ELEMENT_END", CSharpDocLanguage.INSTANCE);
	IElementType XML_TAG_NAME = new IElementType("XML_TAG_NAME", CSharpDocLanguage.INSTANCE);
	IElementType XML_NAME = new IElementType("XML_NAME", CSharpDocLanguage.INSTANCE);
	IElementType XML_ATTRIBUTE_VALUE_TOKEN = new IElementType("XML_ATTRIBUTE_VALUE_TOKEN", CSharpDocLanguage.INSTANCE);
	IElementType XML_ATTRIBUTE_VALUE_START_DELIMITER = new IElementType("XML_ATTRIBUTE_VALUE_START_DELIMITER", CSharpDocLanguage.INSTANCE);
	IElementType XML_ATTRIBUTE_VALUE_END_DELIMITER = new IElementType("XML_ATTRIBUTE_VALUE_END_DELIMITER", CSharpDocLanguage.INSTANCE);
	IElementType XML_EQ = new IElementType("XML_EQ", CSharpDocLanguage.INSTANCE);
	IElementType XML_DATA_CHARACTERS = new IElementType("XML_DATA_CHARACTERS", CSharpDocLanguage.INSTANCE);
	IElementType XML_TAG_CHARACTERS = new IElementType("XML_TAG_CHARACTERS", CSharpDocLanguage.INSTANCE);
	IElementType XML_WHITE_SPACE = TokenType.WHITE_SPACE;
	IElementType XML_REAL_WHITE_SPACE = new IElementType("XML_WHITE_SPACE", CSharpDocLanguage.INSTANCE);
	IElementType XML_COMMENT_START = new IElementType("XML_COMMENT_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_COMMENT_END = new IElementType("XML_COMMENT_END", CSharpDocLanguage.INSTANCE);
	IElementType XML_COMMENT_CHARACTERS = new IElementType("XML_COMMENT_CHARACTERS", CSharpDocLanguage.INSTANCE);

	IElementType TAG_WHITE_SPACE = new IElementType("TAG_WHITE_SPACE", CSharpDocLanguage.INSTANCE);

	IElementType XML_BAD_CHARACTER = new IElementType("XML_BAD_CHARACTER", CSharpDocLanguage.INSTANCE);

	IElementType XML_CONDITIONAL_COMMENT_START = new IElementType("CONDITIONAL_COMMENT_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_CONDITIONAL_COMMENT_START_END = new IElementType("CONDITIONAL_COMMENT_START_END", CSharpDocLanguage.INSTANCE);
	IElementType XML_CONDITIONAL_COMMENT_END_START = new IElementType("CONDITIONAL_COMMENT_END_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_CONDITIONAL_COMMENT_END = new IElementType("CONDITIONAL_COMMENT_END", CSharpDocLanguage.INSTANCE);

	TokenSet COMMENTS = TokenSet.create(XML_COMMENT_START, XML_COMMENT_CHARACTERS, XML_COMMENT_END);
	TokenSet WHITESPACES = TokenSet.create(XML_WHITE_SPACE, TAG_WHITE_SPACE);
}
