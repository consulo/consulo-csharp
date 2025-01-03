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

package consulo.csharp.impl.ide;

import jakarta.annotation.Nonnull;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.codeEditor.HighlighterIterator;
import consulo.language.ast.StringEscapesTokenTypes;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.action.FileQuoteHandler;
import consulo.language.editor.action.JavaLikeQuoteHandler;
import consulo.language.editor.action.SimpleTokenSetQuoteHandler;
import consulo.language.psi.PsiElement;
import consulo.virtualFileSystem.fileType.FileType;

/**
 * @author peter
 * @author VISTALL
 * @see com.intellij.codeInsight.editorActions.JavaQuoteHandler
 *      <p/>
 *      TODO [VISTALL] support consulo.csharp.lang.psi.CSharpTokens#VERBATIM_STRING_LITERAL
 */
@ExtensionImpl
public class CSharpQuoteHandler extends SimpleTokenSetQuoteHandler implements JavaLikeQuoteHandler, FileQuoteHandler
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

				if(!iterator.atEnd() && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.contains((IElementType) iterator.getTokenType()))
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

				if(!iterator.atEnd() && StringEscapesTokenTypes.STRING_LITERAL_ESCAPES.contains((IElementType) iterator.getTokenType()))
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
	public boolean isAppropriateElementTypeForLiteral(final @Nonnull IElementType tokenType)
	{
		return isAppropriateElementTypeForLiteralStatic(tokenType);
	}

	@Override
	public boolean needParenthesesAroundConcatenation(final PsiElement element)
	{
		// example code: "some string".length() must become ("some" + " string").length()
		return element.getParent() instanceof CSharpConstantExpressionImpl && element.getParent().getParent() instanceof CSharpReferenceExpression;
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

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}
