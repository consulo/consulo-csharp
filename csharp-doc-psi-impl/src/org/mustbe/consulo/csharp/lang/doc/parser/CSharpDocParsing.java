/*
 * Copyright 2000-2009 JetBrains s.r.o.
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
package org.mustbe.consulo.csharp.lang.doc.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocElements;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocTokenType;
import org.mustbe.consulo.csharp.lang.doc.validation.CSharpDocAttributeInfo;
import org.mustbe.consulo.csharp.lang.doc.validation.CSharpDocTagInfo;
import org.mustbe.consulo.csharp.lang.doc.validation.CSharpDocTagManager;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.CustomParsingType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.util.containers.Stack;

/**
 * @author max
 * @author VISTALL
 *
 * Base code from XML plugin, {@see com.intellij.psi.impl.source.parsing.xml.XmlParsing}
 */
public class CSharpDocParsing
{
	private static final int BALANCING_DEPTH_THRESHOLD = 1000;

	protected final PsiBuilder myBuilder;
	private final Stack<String> myTagNamesStack = new Stack<String>();

	public CSharpDocParsing(final PsiBuilder builder)
	{
		myBuilder = builder;
	}

	public void parse()
	{
		PsiBuilder.Marker error = null;
		while(!eof())
		{
			final IElementType tt = token();
			if(tt == CSharpDocTokenType.XML_START_TAG_START)
			{
				error = flushError(error);
				parseTag();
			}
			else if(isCommentToken(tt))
			{
				error = flushError(error);
				parseComment();
			}
			else if(tt == CSharpDocTokenType.XML_REAL_WHITE_SPACE ||
					tt == CSharpDocTokenType.XML_DATA_CHARACTERS ||
					tt == CSharpDocTokenType.DOC_LINE_START)
			{
				error = flushError(error);
				advance();
			}
			else
			{
				if(error == null)
				{
					error = mark();
				}
				advance();
			}
		}

		if(error != null)
		{
			error.error("Tag is not closed");
		}
	}

	@Nullable
	private static PsiBuilder.Marker flushError(PsiBuilder.Marker error)
	{
		if(error != null)
		{
			error.error("Unexpected tokens");
			error = null;
		}
		return error;
	}

	protected void parseTag()
	{
		assert token() == CSharpDocTokenType.XML_START_TAG_START : "Tag start expected";
		final PsiBuilder.Marker tag = mark();

		final String tagName = parseTagHeader(tag);
		if(tagName == null)
		{
			return;
		}

		final PsiBuilder.Marker content = mark();
		parseTagContent();

		if(token() == CSharpDocTokenType.XML_END_TAG_START)
		{
			final PsiBuilder.Marker footer = mark();
			advance();

			if(token() == CSharpDocTokenType.XML_NAME)
			{
				String endName = myBuilder.getTokenText();
				if(!tagName.equals(endName) && myTagNamesStack.contains(endName))
				{
					footer.rollbackTo();
					myTagNamesStack.pop();
					tag.doneBefore(CSharpDocElements.TAG, content, "Element '" + tagName + "' is not closed");
					content.drop();
					return;
				}

				advance();
			}
			footer.drop();

			while(token() != CSharpDocTokenType.XML_TAG_END && token() != CSharpDocTokenType.XML_START_TAG_START && token() != CSharpDocTokenType.XML_END_TAG_START &&
					!eof())
			{
				error("Unexpected tokens");
				advance();
			}

			if(token() == CSharpDocTokenType.XML_TAG_END)
			{
				advance();
			}
			else
			{
				error("Closing tag is not done");
			}
		}
		else
		{
			error("Unexpected end of file");
		}

		content.drop();
		myTagNamesStack.pop();
		tag.done(CSharpDocElements.TAG);
	}

	@Nullable
	private String parseTagHeader(final PsiBuilder.Marker tag)
	{
		advance();

		final String tagName;
		if(token() != CSharpDocTokenType.XML_NAME)
		{
			error("Tag name expected");
			tagName = "";
		}
		else
		{
			tagName = myBuilder.getTokenText();
			assert tagName != null;
			advance();
		}
		myTagNamesStack.push(tagName);

		do
		{
			final IElementType tt = token();
			if(tt == CSharpDocTokenType.XML_NAME)
			{
				parseAttribute(tagName);
			}
			else
			{
				break;
			}
		}
		while(true);

		if(token() == CSharpDocTokenType.XML_EMPTY_ELEMENT_END)
		{
			advance();
			myTagNamesStack.pop();
			tag.done(CSharpDocElements.TAG);
			return null;
		}

		if(token() == CSharpDocTokenType.XML_TAG_END)
		{
			advance();
		}
		else
		{
			error("Tag is not closed");
			myTagNamesStack.pop();
			tag.done(CSharpDocElements.TAG);
			return null;
		}

		if(myTagNamesStack.size() > BALANCING_DEPTH_THRESHOLD)
		{
			error("Way too unbalanced. Stopping attempt to balance tags properly at this point");
			tag.done(CSharpDocElements.TAG);
			return null;
		}

		return tagName;
	}

	public void parseTagContent()
	{
		PsiBuilder.Marker xmlText = null;
		while(token() != CSharpDocTokenType.XML_END_TAG_START && !eof())
		{
			final IElementType tt = token();
			if(tt == CSharpDocTokenType.XML_START_TAG_START)
			{
				xmlText = terminateText(xmlText);
				parseTag();
			}
			else if(isCommentToken(tt))
			{
				xmlText = terminateText(xmlText);
				parseComment();
			}
			else if(tt instanceof CustomParsingType || tt instanceof ILazyParseableElementType)
			{
				xmlText = terminateText(xmlText);
				advance();
			}
			else
			{
				xmlText = startText(xmlText);
				advance();
			}
		}

		terminateText(xmlText);
	}

	protected boolean isCommentToken(final IElementType tt)
	{
		return tt == CSharpDocTokenType.XML_COMMENT_START;
	}

	@NotNull
	private PsiBuilder.Marker startText(@Nullable PsiBuilder.Marker xmlText)
	{
		if(xmlText == null)
		{
			xmlText = mark();
			assert xmlText != null;
		}
		return xmlText;
	}

	protected final PsiBuilder.Marker mark()
	{
		return myBuilder.mark();
	}

	@Nullable
	private static PsiBuilder.Marker terminateText(@Nullable PsiBuilder.Marker xmlText)
	{
		if(xmlText != null)
		{
			xmlText.done(CSharpDocElements.TEXT);
			xmlText = null;
		}
		return xmlText;
	}

	protected void parseComment()
	{
		advance();
		while(true)
		{
			final IElementType tt = token();
			if(tt == CSharpDocTokenType.XML_COMMENT_CHARACTERS)
			{
				advance();
				continue;
			}
			else if(tt == CSharpDocTokenType.XML_BAD_CHARACTER)
			{
				final PsiBuilder.Marker error = mark();
				advance();
				error.error("Bad character");
				continue;
			}
			if(tt == CSharpDocTokenType.XML_COMMENT_END)
			{
				advance();
			}
			break;
		}
	}

	private void parseAttribute(String tagInfo)
	{
		String attributeName = myBuilder.getTokenText();

		CSharpDocTagInfo docTagInfo = CSharpDocTagManager.getInstance().getTag(tagInfo);
		CSharpDocAttributeInfo attributeInfo = docTagInfo == null ? null : docTagInfo.getAttribute(attributeName);

		assert token() == CSharpDocTokenType.XML_NAME;
		final PsiBuilder.Marker att = mark();
		advance();
		if(token() == CSharpDocTokenType.XML_EQ)
		{
			advance();
			parseAttributeValue(attributeInfo);
			att.done(CSharpDocElements.ATTRIBUTE);
		}
		else
		{
			error("'=' expected");
			att.done(CSharpDocElements.ATTRIBUTE);
		}
	}

	private void parseAttributeValue(@Nullable CSharpDocAttributeInfo attributeInfo)
	{
		final PsiBuilder.Marker attValue = mark();
		if(token() == CSharpDocTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER)
		{
			advance();

			if(token() == CSharpDocTokenType.XML_BAD_CHARACTER)
			{
				final PsiBuilder.Marker error = mark();
				advance();
				error.error("Unexpected token");
			}
			else if(token() == CSharpDocTokenType.XML_ATTRIBUTE_VALUE_TOKEN)
			{
				if(attributeInfo == null)
				{
					advance();
				}
				else
				{
					switch(attributeInfo.getValueType())
					{
						case REFERENCE:
							myBuilder.remapCurrentToken(CSharpDocElements.TYPE);
							break;
						case PARAMETER:
							myBuilder.remapCurrentToken(CSharpDocElements.PARAMETER_EXPRESSION);
							break;
						case TYPE_PARAMETER:
							myBuilder.remapCurrentToken(CSharpDocElements.GENERIC_PARAMETER_EXPRESSION);
							break;
					}
					advance();
				}
			}

			if(token() == CSharpDocTokenType.XML_ATTRIBUTE_VALUE_END_DELIMITER)
			{
				advance();
			}
			else
			{
				error("Attribute value is not closed");
			}
		}
		else
		{
			error("Attribute value expected");
		}

		attValue.done(CSharpDocElements.ATTRIBUTE_VALUE);
	}

	@Nullable
	protected final IElementType token()
	{
		return myBuilder.getTokenType();
	}

	protected final boolean eof()
	{
		return myBuilder.eof();
	}

	protected final void advance()
	{
		myBuilder.advanceLexer();
	}

	private void error(final String message)
	{
		myBuilder.error(message);
	}
}
