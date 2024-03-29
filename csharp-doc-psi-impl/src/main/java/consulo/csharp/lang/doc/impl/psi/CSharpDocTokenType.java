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

package consulo.csharp.lang.doc.impl.psi;

import consulo.csharp.lang.doc.impl.CSharpDocLanguage;
import consulo.language.ast.TokenSet;
import consulo.language.ast.TokenType;
import consulo.language.ast.IElementType;

/**
 * @author VISTALL
 * @since 03.03.2015
 * <p/>
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
	IElementType DOC_LINE_START = new IElementType("DOC_LINE_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_REAL_WHITE_SPACE = new IElementType("XML_WHITE_SPACE", CSharpDocLanguage.INSTANCE);
	IElementType XML_COMMENT_START = new IElementType("XML_COMMENT_START", CSharpDocLanguage.INSTANCE);
	IElementType XML_COMMENT_END = new IElementType("XML_COMMENT_END", CSharpDocLanguage.INSTANCE);
	IElementType XML_COMMENT_CHARACTERS = new IElementType("XML_COMMENT_CHARACTERS", CSharpDocLanguage.INSTANCE);

	IElementType TAG_WHITE_SPACE = new IElementType("TAG_WHITE_SPACE", CSharpDocLanguage.INSTANCE);

	IElementType XML_BAD_CHARACTER = new IElementType("XML_BAD_CHARACTER", CSharpDocLanguage.INSTANCE);

	TokenSet COMMENTS = TokenSet.create(XML_COMMENT_START, XML_COMMENT_CHARACTERS, XML_COMMENT_END);
	TokenSet WHITESPACES = TokenSet.create(XML_WHITE_SPACE, TAG_WHITE_SPACE, XML_REAL_WHITE_SPACE);
}
