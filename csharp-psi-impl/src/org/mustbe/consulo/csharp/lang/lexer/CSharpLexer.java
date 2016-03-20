/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.lexer;

import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokensImpl;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.LexerPosition;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpLexer extends MergingLexerAdapterBase
{
	private static final TokenSet ourMergeSet = TokenSet.create(CSharpTemplateTokens.MACRO_FRAGMENT, CSharpTokensImpl.LINE_DOC_COMMENT);

	private static class MyMergeFunction implements MergeFunction
	{
		private MyMergeFunction()
		{
		}

		@Override
		public IElementType merge(final IElementType mergeToken, final Lexer originalLexer)
		{
			if(!ourMergeSet.contains(mergeToken))
			{
				return mergeToken;
			}

			while(true)
			{
				IElementType currentToken = originalLexer.getTokenType();
				if(currentToken == null)
				{
					break;
				}

				// we need merge two docs if one line between
				if(mergeToken == CSharpTokensImpl.LINE_DOC_COMMENT && currentToken == CSharpTokens.WHITE_SPACE)
				{
					LexerPosition currentPosition = originalLexer.getCurrentPosition();
					originalLexer.advance();
					boolean docIsNext = originalLexer.getTokenType() == CSharpTokensImpl.LINE_DOC_COMMENT;
					originalLexer.restore(currentPosition);
					if(docIsNext)
					{
						currentToken = CSharpTokensImpl.LINE_DOC_COMMENT;
					}
					else
					{
						break;
					}
				}

				if(currentToken != mergeToken)
				{
					break;
				}

				originalLexer.advance();
			}
			return mergeToken;
		}
	}

	private final MyMergeFunction myMergeFunction;

	public CSharpLexer()
	{
		super(new _CSharpLexer());
		myMergeFunction = new MyMergeFunction();
	}

	@Override
	public MergeFunction getMergeFunction()
	{
		return myMergeFunction;
	}
}
