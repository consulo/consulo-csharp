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
import org.mustbe.consulo.csharp.lang.parser.ModifierSet;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class LinqParsing extends SharedParsingHelpers
{
	public static final TokenSet LINQ_KEYWORDS = TokenSet.create(LET_KEYWORD, FROM_KEYWORD, SELECT_KEYWORD, GROUP_KEYWORD, BY_KEYWORD, INTO_KEYWORD, ORDERBY_KEYWORD, WHERE_KEYWORD,
			ASCENDING_KEYWORD, DESCENDING_KEYWORD, JOIN_KEYWORD, ON_KEYWORD, EQUALS_KEYWORD);

	public static PsiBuilder.Marker parseLinqExpression(final CSharpBuilderWrapper builder, ModifierSet set)
	{
		builder.enableSoftKeywords(LINQ_KEYWORDS);

		try
		{
			PsiBuilder.Marker linqExpressionMarker = builder.mark();

			PsiBuilder.Marker marker = parseFromClause(builder, true, set);
			if(marker == null)
			{
				linqExpressionMarker.rollbackTo();
				return null;
			}

			parseQueryBody(builder, set);

			linqExpressionMarker.done(LINQ_EXPRESSION);
			return linqExpressionMarker;
		}
		finally
		{
			builder.disableSoftKeywords(LINQ_KEYWORDS);
		}
	}

	private static void parseQueryBody(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker queryBody = builder.mark();
		while(!builder.eof())
		{
			PsiBuilder.Marker qMarker = parseQueryBodyClause(builder, set);
			if(qMarker == null)
			{
				break;
			}
		}

		parseSelectOrGroupClause(builder, set);

		parseQueryContinuation(builder, set);

		queryBody.done(LINQ_QUERY_BODY);
	}

	private static void parseQueryContinuation(CSharpBuilderWrapper builder, ModifierSet set)
	{
		if(builder.getTokenType() == CSharpSoftTokens.INTO_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();

			parseIntoClause(builder);

			parseQueryBody(builder, set);

			mark.done(LINQ_QUERY_CONTINUATION);
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseFromClause(final CSharpBuilderWrapper builder, boolean rollback, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		PsiBuilder.Marker variableMarker = builder.mark();

		if(canParseType(builder))
		{
			parseType(builder, BRACKET_RETURN_BEFORE);
		}

		IElementType tokenType = builder.getTokenType();
		if(tokenType == CSharpTokens.IDENTIFIER)
		{
			doneIdentifier(builder, NONE);
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
				variableMarker.error("Identifier expected");
				variableMarker = null;
			}
		}
		if(variableMarker != null)
		{
			variableMarker.done(LINQ_VARIABLE);
		}

		if(builder.getTokenType() == IN_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder, set) == null)
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
		TypeInfo typeInfo = parseType(builder, BRACKET_RETURN_BEFORE);
		if(typeInfo != null)
		{
			if(typeInfo.isParameterized || typeInfo.nativeElementType != null)
			{
				typeInfo.marker.rollbackTo();
				return true;
			}

			if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
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
	private static PsiBuilder.Marker parseQueryBodyClause(CSharpBuilderWrapper builder, ModifierSet set)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == FROM_KEYWORD)
		{
			return parseFromClause(builder, false, set);
		}
		else if(tokenType == WHERE_KEYWORD)
		{
			return parseWhereClause(builder, set);
		}
		else if(tokenType == ORDERBY_KEYWORD)
		{
			return parseOrderByClause(builder, set);
		}
		else if(tokenType == LET_KEYWORD)
		{
			return parseLetClause(builder, set);
		}
		else if(tokenType == JOIN_KEYWORD)
		{
			return parseJoinClause(builder, set);
		}
		return null;
	}

	@Nullable
	private static PsiBuilder.Marker parseJoinClause(final CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		PsiBuilder.Marker variableMarker = builder.mark();

		if(canParseType(builder))
		{
			parseType(builder, BRACKET_RETURN_BEFORE);
		}

		expectOrReportIdentifier(builder, 0);

		variableMarker.done(LINQ_VARIABLE);

		if(builder.getTokenType() == IN_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
		}
		else
		{
			builder.error("'in' expected");
		}

		if(builder.getTokenType() == ON_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
		}
		else
		{
			builder.error("'on' expected");
		}

		if(builder.getTokenType() == EQUALS_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
		}
		else
		{
			builder.error("'equals' expected");
		}

		parseIntoClause(builder);

		mark.done(LINQ_JOIN_CLAUSE);

		return mark;
	}

	private static void parseIntoClause(CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() == INTO_KEYWORD)
		{
			PsiBuilder.Marker tempMarker = builder.mark();
			builder.advanceLexer();

			PsiBuilder.Marker varMarker = builder.mark();
			expectOrReportIdentifier(builder, NONE);
			varMarker.done(LINQ_VARIABLE);

			tempMarker.done(LINQ_INTRO_CLAUSE);
		}
	}

	private static PsiBuilder.Marker parseWhereClause(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(ExpressionParsing.parse(builder, set) == null)
		{
			builder.error("Expression expected");
		}
		mark.done(LINQ_WHERE_CLAUSE);
		return mark;
	}

	private static PsiBuilder.Marker parseLetClause(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
		{
			PsiBuilder.Marker varMarker = builder.mark();
			doneIdentifier(builder, NONE);
			if(expect(builder, EQ, "'=' expected"))
			{
				if(ExpressionParsing.parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
			}
			varMarker.done(LINQ_VARIABLE);
		}
		else
		{
			builder.error("Identifier expected");
		}

		mark.done(LINQ_LET_CLAUSE);
		return mark;
	}

	private static PsiBuilder.Marker parseOrderByClause(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		while(!builder.eof())
		{
			PsiBuilder.Marker subMarker = builder.mark();
			if(ExpressionParsing.parse(builder, set) == null)
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

	private static void parseSelectOrGroupClause(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(builder.getTokenType() == SELECT_KEYWORD)
		{
			builder.advanceLexer();

			if(ExpressionParsing.parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
		}
		else if(builder.getTokenType() == GROUP_KEYWORD)
		{
			builder.advanceLexer();
			if(ExpressionParsing.parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
			if(builder.getTokenType() == BY_KEYWORD)
			{
				builder.advanceLexer();
				if(ExpressionParsing.parse(builder, set) == null)
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
