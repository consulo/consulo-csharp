/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package org.mustbe.consulo.csharp.ide;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import com.intellij.codeInsight.editorActions.JavaLikeQuoteHandler;
import com.intellij.codeInsight.editorActions.SimpleTokenSetQuoteHandler;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StringEscapesTokenTypes;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author peter
 * @author VISTALL
 * @see com.intellij.codeInsight.editorActions.JavaQuoteHandler
 *      <p/>
 *      TODO [VISTALL] support org.mustbe.consulo.csharp.lang.psi.CSharpTokens#VERBATIM_STRING_LITERAL
 */
public class CSharpQuoteHandler extends SimpleTokenSetQuoteHandler implements JavaLikeQuoteHandler
{
	private final TokenSet concatenatableStrings;

	public CSharpQuoteHandler()
	{
		super(CSharpTokens.STRING_LITERAL, CSharpTokens.CHARACTER_LITERAL);
		concatenatableStrings = TokenSet.create(CSharpTokens.STRING_LITERAL);
	}

	@Override
	public boolean isOpeningQuote(HighlighterIterator iterator, int offset)
	{
		boolean openingQuote = super.isOpeningQuote(iterator, offset);

		if(openingQuote)
		{
			// check escape next
			if(!iterator.atEnd())
			{
				iterator.retreat();

				if(!iterator.atEnd() && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.contains(iterator.getTokenType()))
				{
					openingQuote = false;
				}
				iterator.advance();
			}
		}
		return openingQuote;
	}

	@Override
	public boolean isClosingQuote(HighlighterIterator iterator, int offset)
	{
		boolean closingQuote = super.isClosingQuote(iterator, offset);

		if(closingQuote)
		{
			// check escape next
			if(!iterator.atEnd())
			{
				iterator.advance();

				if(!iterator.atEnd() && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.contains(iterator.getTokenType()))
				{
					closingQuote = false;
				}
				iterator.retreat();
			}
		}
		return closingQuote;
	}

	@Override
	public TokenSet getConcatenatableStringTokenTypes()
	{
		return concatenatableStrings;
	}

	@Override
	public String getStringConcatenationOperatorRepresentation()
	{
		return "+";
	}

	@Override
	public TokenSet getStringTokenTypes()
	{
		return myLiteralTokenSet;
	}

	@Override
	public boolean isAppropriateElementTypeForLiteral(final @NotNull IElementType tokenType)
	{
		return isAppropriateElementTypeForLiteralStatic(tokenType);
	}

	@Override
	public boolean needParenthesesAroundConcatenation(final PsiElement element)
	{
		// example code: "some string".length() must become ("some" + " string").length()
		return element.getParent() instanceof CSharpConstantExpressionImpl && element.getParent().getParent() instanceof
				CSharpReferenceExpressionImpl;
	}

	public static boolean isAppropriateElementTypeForLiteralStatic(final IElementType tokenType)
	{
		return CSharpTokenSets.COMMENTS.contains(tokenType) ||
				tokenType == CSharpTokens.WHITE_SPACE ||
				tokenType == CSharpTokens.SEMICOLON ||
				tokenType == CSharpTokens.COMMA ||
				tokenType == CSharpTokens.RPAR ||
				tokenType == CSharpTokens.RBRACKET ||
				tokenType == CSharpTokens.RBRACE ||
				tokenType == CSharpTokens.STRING_LITERAL ||
				tokenType == CSharpTokens.CHARACTER_LITERAL;
	}
}
