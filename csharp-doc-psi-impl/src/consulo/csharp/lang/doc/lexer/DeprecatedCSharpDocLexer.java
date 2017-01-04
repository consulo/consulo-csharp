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

package consulo.csharp.lang.doc.lexer;

import consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LookAheadLexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.09.14
 *
 * TODO [VISTALL] merge with CSharpDocLexer
 */
public class DeprecatedCSharpDocLexer extends LookAheadLexer
{
	private boolean myNewLine = true;

	public DeprecatedCSharpDocLexer()
	{
		super(new CSharpDocLexer());
	}

	@Override
	protected void lookAhead(Lexer baseLexer)
	{
		IElementType tokenType = baseLexer.getTokenType();
		if(tokenType == CSharpDocTokenType.TAG_WHITE_SPACE || tokenType == CSharpDocTokenType.XML_REAL_WHITE_SPACE)
		{
			CharSequence tokenSequence = baseLexer.getTokenSequence();
			if(StringUtil.containsLineBreak(tokenSequence))
			{
				myNewLine = true;
				super.lookAhead(baseLexer);
				return;
			}
		}

		if(myNewLine)
		{
			if(tokenType == CSharpDocTokenType.XML_DATA_CHARACTERS)
			{
				CharSequence tokenSequence = baseLexer.getTokenSequence();
				if(StringUtil.equals(tokenSequence, "///"))
				{
					myNewLine = false;
					advanceAs(baseLexer, CSharpDocTokenType.DOC_LINE_START);
					return;
				}
			}
		}

		super.lookAhead(baseLexer);
	}
}
