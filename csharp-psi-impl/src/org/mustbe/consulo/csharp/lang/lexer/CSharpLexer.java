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

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokensImpl;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.Lexer;
import com.intellij.lexer.MergeFunction;
import com.intellij.lexer.MergingLexerAdapterBase;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import lombok.val;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpLexer extends MergingLexerAdapterBase
{
	private static final TokenSet ourMergeSet = TokenSet.create(CSharpTemplateTokens.MACRO_FRAGMENT, CSharpTokens.NON_ACTIVE_SYMBOL,
			CSharpTokensImpl.LINE_DOC_COMMENT);

	private static class MyMergeFunction implements MergeFunction
	{
		private List<TextRange> myRanges;

		private MyMergeFunction(List<TextRange> ranges)
		{
			myRanges = ranges;
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
				IElementType currentToken = modifyNonActiveSymbols(originalLexer, myRanges);

				// we need merge two docs if one line between
				if(mergeToken == CSharpTokensImpl.LINE_DOC_COMMENT && currentToken == CSharpTokens.WHITE_SPACE)
				{
					if(hasOnlyOneLine(originalLexer.getTokenSequence()))
					{
						val currentPosition = originalLexer.getCurrentPosition();
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
				}

				if(currentToken != mergeToken)
				{
					break;
				}

				originalLexer.advance();
			}
			return mergeToken;
		}

		@Nullable
		private static IElementType modifyNonActiveSymbols(Lexer originalLexer, List<TextRange> textRanges)
		{
			IElementType tokenType = originalLexer.getTokenType();
			if(tokenType == null)
			{
				return null;
			}

			if(textRanges.isEmpty())
			{
				return tokenType;
			}

			for(int i = 0; i < textRanges.size(); i++)
			{
				TextRange textRange = textRanges.get(i);
				if(textRange.contains(originalLexer.getTokenStart()))
				{
					return CSharpTokens.NON_ACTIVE_SYMBOL;
				}
			}
			return tokenType;
		}

		private static boolean hasOnlyOneLine(CharSequence sequence)
		{
			int c = 0;
			int len = sequence.length();
			if(len == 0)
			{
				return false;
			}
			for(int i = 0; i < len; i++)
			{
				if(sequence.charAt(i) == '\n')
				{
					c++;
					if(c == 2)
					{
						return false;
					}
				}
			}
			return c == 1;
		}
	}

	private final MyMergeFunction myMergeFunction;

	public CSharpLexer()
	{
		this(Collections.<TextRange>emptyList());
	}

	public CSharpLexer(List<TextRange> ranges)
	{
		super(new FlexAdapter(new _CSharpLexer()));
		myMergeFunction = new MyMergeFunction(ranges);
	}

	@Override
	public MergeFunction getMergeFunction()
	{
		return myMergeFunction;
	}
}
