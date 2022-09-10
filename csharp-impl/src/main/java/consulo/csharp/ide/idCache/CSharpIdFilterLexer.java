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

package consulo.csharp.ide.idCache;

import consulo.language.ast.TokenType;
import consulo.language.lexer.Lexer;
import consulo.language.psi.stub.BaseFilterLexer;
import consulo.language.psi.search.UsageSearchContext;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.psi.stub.OccurrenceConsumer;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public class CSharpIdFilterLexer extends BaseFilterLexer
{
	private static final TokenSet ourSkipWordsScanSet = TokenSet.orSet(TokenSet.create(TokenType.WHITE_SPACE, CSharpTokens.LPAR, CSharpTokens.RPAR, CSharpTokens.LBRACE, CSharpTokens.RBRACE,
			CSharpTokens.LBRACKET, CSharpTokens.RBRACKET, CSharpTokens.SEMICOLON, CSharpTokens.COMMA, CSharpTokens.DOT), CSharpTokenSets.OVERLOADING_OPERATORS);

	public CSharpIdFilterLexer(Lexer originalLexer, OccurrenceConsumer occurrenceConsumer)
	{
		super(originalLexer, occurrenceConsumer);
	}

	@Override
	public void advance()
	{
		final IElementType tokenType = myDelegate.getTokenType();

		if(tokenType == CSharpTokens.IDENTIFIER)
		{
			addOccurrenceInToken(UsageSearchContext.IN_CODE);
			CharSequence tokenSequence = myDelegate.getTokenSequence();
			if(tokenSequence.charAt(0) == '@')
			{
				scanWordsInToken(UsageSearchContext.IN_CODE, false, false);
			}
		}
		else if(tokenType == CSharpTokens.LONG_LITERAL || tokenType == CSharpTokens.INTEGER_LITERAL || tokenType == CSharpTokens.CHARACTER_LITERAL || tokenType == CSharpTokens.ARROW || tokenType ==
				CSharpTokens.DARROW)
		{
			addOccurrenceInToken(UsageSearchContext.IN_CODE);
		}
		else if(CSharpTokenSets.STRINGS.contains(tokenType))
		{
			scanWordsInToken(UsageSearchContext.IN_STRINGS | UsageSearchContext.IN_FOREIGN_LANGUAGES, false, true);
		}
		else if(CSharpTokenSets.COMMENTS.contains(tokenType))
		{
			scanWordsInToken(UsageSearchContext.IN_COMMENTS, false, false);
			advanceTodoItemCountsInToken();
		}
		else if(!ourSkipWordsScanSet.contains(tokenType))
		{
			scanWordsInToken(UsageSearchContext.IN_PLAIN_TEXT, false, false);
		}

		myDelegate.advance();
	}
}
