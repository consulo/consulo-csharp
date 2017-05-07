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

import java.lang.annotation.ElementType;

import com.intellij.lexer.Lexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.cache.impl.BaseFilterLexer;
import com.intellij.psi.impl.cache.impl.OccurrenceConsumer;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;

/**
 * @author VISTALL
 * @since 07-May-17
 */
public class CSharpIdFilterLexer extends BaseFilterLexer
{
	private static final TokenSet ourSkipWordsScanSet = TokenSet.orSet(TokenSet.create(TokenType.WHITE_SPACE,
			CSharpTokens.LPAR,
			CSharpTokens.RPAR,
			CSharpTokens.LBRACE,
			CSharpTokens.RBRACE,
			CSharpTokens.LBRACKET,
			CSharpTokens.RBRACKET,
			CSharpTokens.SEMICOLON,
			CSharpTokens.COMMA,
			CSharpTokens.DOT), CSharpTokenSets.OVERLOADING_OPERATORS);

	public CSharpIdFilterLexer(Lexer originalLexer, OccurrenceConsumer occurrenceConsumer)
	{
		super(originalLexer, occurrenceConsumer);
	}

	@Override
	public void advance()
	{
		final IElementType tokenType = myDelegate.getTokenType();

		if(tokenType == CSharpTokens.IDENTIFIER || tokenType == CSharpTokens.LONG_LITERAL || tokenType == CSharpTokens.INTEGER_LITERAL || tokenType == CSharpTokens.CHARACTER_LITERAL || tokenType ==
				CSharpTokens.ARROW || tokenType == CSharpTokens.DARROW)
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
