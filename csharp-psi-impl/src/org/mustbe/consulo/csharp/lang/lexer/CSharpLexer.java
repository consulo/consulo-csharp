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

import java.io.Reader;
import java.util.Collections;
import java.util.List;

import org.mustbe.consulo.csharp.lang.psi.CSharpTemplateTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lexer.FlexAdapter;
import com.intellij.lexer.MergingLexerAdapter;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpLexer extends MergingLexerAdapter
{
	private static final TokenSet ourMergeSet = TokenSet.create(CSharpTemplateTokens.MACRO_FRAGMENT, CSharpTokens.NON_ACTIVE_SYMBOL);
	private final List<TextRange> myRanges;

	public CSharpLexer()
	{
		this(Collections.<TextRange>emptyList());
	}

	public CSharpLexer(List<TextRange> ranges)
	{
		super(new FlexAdapter(new _CSharpLexer((Reader) null)), ourMergeSet);
		myRanges = ranges;
	}

	@Override
	public IElementType getTokenType()
	{
		IElementType tokenType = super.getTokenType();
		if(tokenType == null)
		{
			return null;
		}

		if(myRanges.isEmpty())
		{
			return tokenType;
		}

		for(int i = 0; i < myRanges.size(); i++)
		{
			TextRange textRange = myRanges.get(i);
			if(textRange.contains(getTokenStart()))
			{
				return CSharpTokens.NON_ACTIVE_SYMBOL;
			}
		}
		return tokenType;
	}
}
