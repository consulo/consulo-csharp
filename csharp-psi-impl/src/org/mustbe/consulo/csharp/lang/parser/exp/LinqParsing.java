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

package org.mustbe.consulo.csharp.lang.parser.exp;

import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class LinqParsing extends SharingParsingHelpers
{
	public static final TokenSet LINQ_KEYWORDS = TokenSet.create(LET_KEYWORD, FROM_KEYWORD, SELECT_KEYWORD, GROUP_KEYWORD, BY_KEYWORD,
			INTO_KEYWORD, ORDERBY_KEYWORD, WHERE_KEYWORD);

	public static PsiBuilder.Marker parseLinqExpression(final CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() != FROM_KEYWORD)
		{
			return null;
		}

		PsiBuilder.Marker mark = builder.mark();

		doneFrom(builder);

		while(!builder.eof())
		{
			if(builder.getTokenType() == FROM_KEYWORD)
			{
				doneFrom(builder);
			}
			else if(builder.getTokenType() == LET_KEYWORD)
			{
				doneElementWithOneExpression(builder, LINQ_LET);
			}
			else
			{
				break;
			}
		}

		if(builder.getTokenType() == WHERE_KEYWORD)
		{
			doneElementWithOneExpression(builder, LINQ_WHERE);
		}

		if(builder.getTokenType() == SELECT_KEYWORD)
		{
			doneElementWithOneExpression(builder, LINQ_SELECT);
		}
		else
		{
			builder.error("'select' expected");
		}

		mark.done(LINQ_EXPRESSION);
		return mark;
	}

	// from <EXP> in <EXP>
	private static void doneFrom(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(ExpressionParsing.parse(builder) == null)
		{
			builder.error("Expression expected");
		}

		if(builder.getTokenType() == IN_KEYWORD)
		{
			doneElementWithOneExpression(builder, LINQ_IN);
		}
		else
		{
			builder.error("'in' expected");
		}
		mark.done(LINQ_FROM);
	}

	private static void doneElementWithOneExpression(CSharpBuilderWrapper builder, IElementType elementType)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(ExpressionParsing.parse(builder) == null)
		{
			builder.error("Expression expected");
		}
		mark.done(elementType);
	}
}
