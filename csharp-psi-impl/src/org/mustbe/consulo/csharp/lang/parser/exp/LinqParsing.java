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

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class LinqParsing extends SharedParsingHelpers
{
	public static final TokenSet LINQ_KEYWORDS = TokenSet.create(LET_KEYWORD, FROM_KEYWORD, SELECT_KEYWORD, GROUP_KEYWORD, BY_KEYWORD, INTO_KEYWORD,
			ORDERBY_KEYWORD, WHERE_KEYWORD, ASCENDING_KEYWORD, DESCENDING_KEYWORD);

	public static PsiBuilder.Marker parseLinqExpression(final CSharpBuilderWrapper builder)
	{
		builder.enableSoftKeywords(LINQ_KEYWORDS);

		try
		{
			PsiBuilder.Marker linqExpressionMarker = builder.mark();

			PsiBuilder.Marker marker = parseFromClause(builder, true);
			if(marker == null)
			{
				linqExpressionMarker.rollbackTo();
				return null;
			}

			PsiBuilder.Marker queryBody = builder.mark();
			while(!builder.eof())
			{
				PsiBuilder.Marker qMarker = parseQueryBodyClause(builder);
				if(qMarker == null)
				{
					break;
				}
			}
			//TODO [VISTALL] QueryBodyClause*

			parseSelectOrGroupClause(builder);

			//TODO [VISTALL] QueryContinuation?

			queryBody.done(LINQ_QUERY_BODY);

			linqExpressionMarker.done(LINQ_EXPRESSION);
			return linqExpressionMarker;
		}
		finally
		{
			builder.disableSoftKeywords(LINQ_KEYWORDS);
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseFromClause(final CSharpBuilderWrapper builder, boolean rollback)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		PsiBuilder.Marker variableMarker = builder.mark();

		if(canParseType(builder))
		{
			parseType(builder, BracketFailPolicy.RETURN_BEFORE, false);
		}

		if(builder.getTokenType() == IDENTIFIER)
		{
			builder.advanceLexer();
		}
		else
		{
			if(rollback)
			{
				variableMarker.rollbackTo();
				mark.rollbackTo();
				return null;
			}
			else
			{
				builder.error("Identifier expected");
			}
		}
		variableMarker.done(LINQ_VARIABLE);

		if(builder.getTokenType() == IN_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder) == null)
			{
				builder.error("Expression expected");
			}
		}
		else
		{
			builder.error("'in' expected");
		}

		mark.done(LINQ_FROM_CLAUSE);

		return mark;
	}

	private static boolean canParseType(CSharpBuilderWrapper builder)
	{
		TypeInfo typeInfo = parseType(builder, BracketFailPolicy.RETURN_BEFORE, false);
		if(typeInfo != null)
		{
			if(typeInfo.isParameterized || typeInfo.nativeElementType != null)
			{
				typeInfo.marker.rollbackTo();
				return true;
			}

			if(builder.getTokenType() == IDENTIFIER)
			{
				typeInfo.marker.rollbackTo();
				return true;
			}
			typeInfo.marker.rollbackTo();
			return false;
		}
		else
		{
			return false;
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseQueryBodyClause(CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() == FROM_KEYWORD)
		{
			return parseFromClause(builder, false);
		}
		else if(builder.getTokenType() == WHERE_KEYWORD)
		{
			return parseWhereClause(builder);
		}
		else if(builder.getTokenType() == ORDERBY_KEYWORD)
		{
			return parseOrderByClause(builder);
		}
		return null;
	}

	private static PsiBuilder.Marker parseWhereClause(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(ExpressionParsing.parse(builder) == null)
		{
			builder.error("Expression expected");
		}
		mark.done(LINQ_WHERE_CLAUSE);
		return mark;
	}

	private static PsiBuilder.Marker parseOrderByClause(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		while(!builder.eof())
		{
			PsiBuilder.Marker subMarker = builder.mark();
			if(ExpressionParsing.parse(builder) == null)
			{
				subMarker.drop();
				subMarker = null;
				builder.error("Expression expected");
			}

			if(builder.getTokenType() == ASCENDING_KEYWORD || builder.getTokenType() == DESCENDING_KEYWORD)
			{
				builder.advanceLexer();
			}
			if(subMarker != null)
			{
				subMarker.done(LINQ_ORDERBY_ORDERING);
			}

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else
			{
				break;
			}
		}

		mark.done(LINQ_ORDERBY_CLAUSE);
		return mark;
	}

	private static void parseSelectOrGroupClause(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(builder.getTokenType() == SELECT_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder) == null)
			{
				builder.error("Expression expected");
			}
		}
		else if(builder.getTokenType() == GROUP_KEYWORD)
		{
			builder.advanceLexer();
			if(ExpressionParsing.parse(builder) == null)
			{
				builder.error("Expression expected");
			}
			if(builder.getTokenType() == BY_KEYWORD)
			{
				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}
			else
			{
				builder.error("'by' expected");
			}
		}
		else
		{
			mark.error("'select' or 'group' expected");
			return;
		}
		mark.done(LINQ_SELECT_OR_GROUP_CLAUSE);
	}
}
