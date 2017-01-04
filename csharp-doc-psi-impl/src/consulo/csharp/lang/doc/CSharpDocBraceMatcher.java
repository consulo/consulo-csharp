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

package consulo.csharp.lang.doc;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import com.intellij.codeInsight.highlighting.XmlAwareBraceMatcher;
import com.intellij.lang.BracePair;
import com.intellij.lang.LanguageBraceMatching;
import com.intellij.lang.PairedBraceMatcher;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.BidirectionalMap;

/**
 * @author Maxim.Mossienko
 * @see com.intellij.xml.impl.XmlBraceMatcher
 */
public class CSharpDocBraceMatcher implements XmlAwareBraceMatcher, PairedBraceMatcher
{
	private static final int XML_TAG_TOKEN_GROUP = 1;
	private static final int XML_VALUE_DELIMITER_GROUP = 2;

	private static final BidirectionalMap<IElementType, IElementType> PAIRING_TOKENS = new BidirectionalMap<IElementType, IElementType>();

	static
	{
		PAIRING_TOKENS.put(CSharpDocTokenType.XML_TAG_END, CSharpDocTokenType.XML_START_TAG_START);
		PAIRING_TOKENS.put(CSharpDocTokenType.XML_EMPTY_ELEMENT_END, CSharpDocTokenType.XML_START_TAG_START);
		PAIRING_TOKENS.put(CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, CSharpDocTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER);
	}

	@Override
	public int getBraceTokenGroupId(IElementType tokenType)
	{
		return tokenType == CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER || tokenType == CSharpDocTokenType
				.XML_ATTRIBUTE_VALUE_END_DELIMITER ? XML_VALUE_DELIMITER_GROUP : XML_TAG_TOKEN_GROUP;
	}

	@Override
	public boolean isLBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType)
	{
		final IElementType tokenType = iterator.getTokenType();

		return tokenType == CSharpDocTokenType.XML_START_TAG_START ||
				tokenType == CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER;
	}

	@Override
	public boolean isRBraceToken(HighlighterIterator iterator, CharSequence fileText, FileType fileType)
	{
		final IElementType tokenType = iterator.getTokenType();

		if(tokenType == CSharpDocTokenType.XML_EMPTY_ELEMENT_END ||
				tokenType == CSharpDocTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER)
		{
			return true;
		}
		else if(tokenType == CSharpDocTokenType.XML_TAG_END)
		{
			return findEndTagStart(iterator);
		}
		else
		{
			return false;
		}
	}

	@Override
	public boolean isPairBraces(IElementType tokenType1, IElementType tokenType2)
	{
		if(tokenType2.equals(PAIRING_TOKENS.get(tokenType1)))
		{
			return true;
		}
		List<IElementType> keys = PAIRING_TOKENS.getKeysByValue(tokenType1);
		return keys != null && keys.contains(tokenType2);
	}

	@Override
	public boolean isStructuralBrace(HighlighterIterator iterator, CharSequence text, FileType fileType)
	{
		IElementType tokenType = iterator.getTokenType();
		return isXmlStructuralBrace(iterator, text, fileType, tokenType);
	}

	protected boolean isXmlStructuralBrace(HighlighterIterator iterator, CharSequence text, FileType fileType, IElementType tokenType)
	{
		return tokenType == CSharpDocTokenType.XML_START_TAG_START ||
				tokenType == CSharpDocTokenType.XML_TAG_END ||
				tokenType == CSharpDocTokenType.XML_EMPTY_ELEMENT_END;
	}

	@Override
	public BracePair[] getPairs()
	{
		return new BracePair[0];
	}

	@Override
	public boolean isPairedBracesAllowedBeforeType(@NotNull final IElementType lbraceType, @Nullable final IElementType contextType)
	{
		return true;
	}

	@Override
	public boolean isStrictTagMatching(final FileType fileType, final int braceGroupId)
	{
		switch(braceGroupId)
		{
			case XML_TAG_TOKEN_GROUP:
				// Other xml languages may have nonbalanced tag names
				return isStrictTagMatchingForFileType(fileType);

			default:
				return false;
		}
	}

	protected boolean isStrictTagMatchingForFileType(final FileType fileType)
	{
		return true; //fileType == XmlFileType.INSTANCE || fileType == XHtmlFileType.INSTANCE;
	}

	@Override
	public boolean areTagsCaseSensitive(final FileType fileType, final int braceGroupId)
	{
		switch(braceGroupId)
		{
			case XML_TAG_TOKEN_GROUP:
				return true;//fileType == XmlFileType.INSTANCE;
			default:
				return false;
		}
	}

	private static boolean findEndTagStart(HighlighterIterator iterator)
	{
		IElementType tokenType = iterator.getTokenType();
		int balance = 0;
		int count = 0;
		while(balance >= 0)
		{
			iterator.retreat();
			count++;
			if(iterator.atEnd())
			{
				break;
			}
			tokenType = iterator.getTokenType();
			if(tokenType == CSharpDocTokenType.XML_TAG_END || tokenType == CSharpDocTokenType.XML_EMPTY_ELEMENT_END)
			{
				balance++;
			}
			else if(tokenType == CSharpDocTokenType.XML_END_TAG_START || tokenType == CSharpDocTokenType.XML_START_TAG_START)
			{
				balance--;
			}
		}
		while(count-- > 0)
		{
			iterator.advance();
		}
		return tokenType == CSharpDocTokenType.XML_END_TAG_START;
	}

	@Override
	public String getTagName(CharSequence fileText, HighlighterIterator iterator)
	{
		final IElementType tokenType = iterator.getTokenType();
		String name = null;
		if(tokenType == CSharpDocTokenType.XML_START_TAG_START)
		{
			iterator.advance();
			IElementType tokenType1 = iterator.atEnd() ? null : iterator.getTokenType();

			boolean wasWhiteSpace = false;
			if(isWhitespace(tokenType1))
			{
				wasWhiteSpace = true;
				iterator.advance();
				tokenType1 = iterator.atEnd() ? null : iterator.getTokenType();
			}

			if(tokenType1 == CSharpDocTokenType.XML_TAG_NAME || tokenType1 == CSharpDocTokenType.XML_NAME)
			{
				name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
			}

			if(wasWhiteSpace)
			{
				iterator.retreat();
			}
			iterator.retreat();
		}
		else if(tokenType == CSharpDocTokenType.XML_TAG_END || tokenType == CSharpDocTokenType.XML_EMPTY_ELEMENT_END)
		{
			int balance = 0;
			int count = 0;
			IElementType tokenType1 = iterator.getTokenType();
			while(balance >= 0)
			{
				iterator.retreat();
				count++;
				if(iterator.atEnd())
				{
					break;
				}
				tokenType1 = iterator.getTokenType();

				if(tokenType1 == CSharpDocTokenType.XML_TAG_END || tokenType1 == CSharpDocTokenType.XML_EMPTY_ELEMENT_END)
				{
					balance++;
				}
				else if(tokenType1 == CSharpDocTokenType.XML_TAG_NAME)
				{
					balance--;
				}
			}
			if(tokenType1 == CSharpDocTokenType.XML_TAG_NAME)
			{
				name = fileText.subSequence(iterator.getStart(), iterator.getEnd()).toString();
			}
			while(count-- > 0)
			{
				iterator.advance();
			}
		}

		return name;
	}

	protected boolean isWhitespace(final IElementType tokenType1)
	{
		return tokenType1 == TokenType.WHITE_SPACE;
	}

	@Override
	public IElementType getOppositeBraceTokenType(@NotNull final IElementType type)
	{
		PairedBraceMatcher matcher = LanguageBraceMatching.INSTANCE.forLanguage(type.getLanguage());
		if(matcher != null)
		{
			BracePair[] pairs = matcher.getPairs();
			for(BracePair pair : pairs)
			{
				if(pair.getLeftBraceType() == type)
				{
					return pair.getRightBraceType();
				}
				if(pair.getRightBraceType() == type)
				{
					return pair.getLeftBraceType();
				}
			}
		}
		return null;
	}

	@Override
	public int getCodeConstructStart(final PsiFile file, int openingBraceOffset)
	{
		return openingBraceOffset;
	}
}
