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

package consulo.csharp.lang.doc.impl.lexer;

import consulo.csharp.lang.doc.impl.psi.CSharpDocTokenType;
import consulo.language.ast.TokenSet;
import consulo.language.lexer.MergingLexerAdapter;
import consulo.language.lexer.Lexer;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocLexer extends MergingLexerAdapter
{
	private final static TokenSet TOKENS_TO_MERGE = TokenSet.create(CSharpDocTokenType.XML_DATA_CHARACTERS, CSharpDocTokenType.XML_TAG_CHARACTERS,
			CSharpDocTokenType.XML_ATTRIBUTE_VALUE_TOKEN, CSharpDocTokenType.XML_COMMENT_CHARACTERS);

	public CSharpDocLexer()
	{
		this(new _CSharpDocLexer());
	}

	public CSharpDocLexer(Lexer baseLexer)
	{
		super(baseLexer, TOKENS_TO_MERGE);
	}
}

