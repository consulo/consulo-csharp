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

package org.mustbe.consulo.csharp.lang.parser.stmt;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.decl.FieldOrPropertyParsing;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.parser.exp.LinqParsing;
import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullFunction;
import lombok.val;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class StatementParsing extends SharingParsingHelpers
{
	private static final TokenSet BODY_SOFT_KEYWORDS = TokenSet.orSet(TokenSet.create(YIELD_KEYWORD), LinqParsing.LINQ_KEYWORDS);

	public static PsiBuilder.Marker parse(CSharpBuilderWrapper wrapper)
	{
		return parseWithSoftElements(new NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, Boolean>>()
		{
			@NotNull
			@Override
			public Pair<PsiBuilder.Marker, Boolean> fun(CSharpBuilderWrapper builderWrapper)
			{
				return new Pair<PsiBuilder.Marker, Boolean>(parseStatement(builderWrapper), Boolean.TRUE);
			}
		}, wrapper, BODY_SOFT_KEYWORDS).getFirst();
	}

	private static PsiBuilder.Marker parseStatement(CSharpBuilderWrapper wrapper)
	{
		val marker = wrapper.mark();

		val tokenType = wrapper.getTokenType();

		if(tokenType == LOCK_KEYWORD)
		{
			parseStatementWithParenthesesExpression(wrapper, marker, LOCK_STATEMENT);
		}
		else if(tokenType == BREAK_KEYWORD)
		{
			wrapper.advanceLexer();

			expect(wrapper, SEMICOLON, "';' expected");

			marker.done(BREAK_STATEMENT);
		}
		else if(tokenType == GOTO_KEYWORD)
		{
			wrapper.advanceLexer();

			doneOneElement(wrapper, IDENTIFIER, REFERENCE_EXPRESSION, "Identifier expected");

			expect(wrapper, SEMICOLON, "';' expected");

			marker.done(GOTO_STATEMENT);
		}
		else if(tokenType == CONTINUE_KEYWORD)
		{
			wrapper.advanceLexer();

			expect(wrapper, SEMICOLON, "';' expected");

			marker.done(CONTINUE_STATEMENT);
		}
		else if(tokenType == FOR_KEYWORD)
		{
			parseForStatement(wrapper, marker);
		}
		else if(tokenType == DO_KEYWORD)
		{
			parseDoWhileStatement(wrapper, marker);
		}
		else if(tokenType == RETURN_KEYWORD)
		{
			parseReturnStatement(wrapper, marker);
		}
		else if(tokenType == THROW_KEYWORD)
		{
			parseThrowStatement(wrapper, marker);
		}
		else if(tokenType == FOREACH_KEYWORD)
		{
			parseForeach(wrapper, marker);
		}
		else if(tokenType == YIELD_KEYWORD)
		{
			parseYieldStatement(wrapper, marker);
		}
		else if(tokenType == TRY_KEYWORD)
		{
			parseTryStatement(wrapper, marker);
		}
		else if(tokenType == CATCH_KEYWORD)
		{
			parseCatchStatement(wrapper, marker);
		}
		else if(tokenType == FINALLY_KEYWORD)
		{
			parseFinallyStatement(wrapper, marker);
		}
		else if(tokenType == IF_KEYWORD)
		{
			parseIfStatement(wrapper, marker);
		}
		else if(tokenType == LBRACE)
		{
			parseBlockStatement(wrapper, marker);
		}
		else if(tokenType == WHILE_KEYWORD)
		{
			parseStatementWithParenthesesExpression(wrapper, marker, WHILE_STATEMENT);
		}
		else if(tokenType == CHECKED_KEYWORD || tokenType == UNCHECKED_KEYWORD)
		{
			parseCheckedStatement(wrapper, marker);
		}
		else if(tokenType == USING_KEYWORD)
		{
			parseUsingOrFixed(wrapper, marker, USING_STATEMENT);
		}
		else if(tokenType == FIXED_KEYWORD)
		{
			parseUsingOrFixed(wrapper, marker, FIXED_STATEMENT);
		}
		else if(tokenType == CASE_KEYWORD || tokenType == DEFAULT_KEYWORD && wrapper.lookAhead(1) == COLON)  // accept with colon only,
		// default can be expression
		{
			parseSwitchLabel(wrapper, marker, tokenType == CASE_KEYWORD);
		}
		else if(tokenType == SWITCH_KEYWORD)
		{
			parseSwitch(wrapper, marker);
		}
		else if(tokenType == CONST_KEYWORD)
		{
			PsiBuilder.Marker varMark = wrapper.mark();

			wrapper.advanceLexer();

			FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(wrapper, varMark, LOCAL_VARIABLE);

			expect(wrapper, SEMICOLON, "';' expected");

			marker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
		}
		else if(tokenType == SEMICOLON)
		{
			wrapper.advanceLexer();
			marker.done(EMPTY_STATEMENT);
		}
		else
		{
			if(tokenType == IDENTIFIER && wrapper.lookAhead(1) == COLON)
			{
				parseLabeledStatement(wrapper, marker);
				return marker;
			}

			if(parseVariableOrExpression(wrapper, marker) == null)
			{
				return null;
			}
		}
		return marker;
	}

	private static enum ParseVariableOrExpressionResult
	{
		VARIABLE,
		EXPRESSION
	}

	@Nullable
	private static ParseVariableOrExpressionResult parseVariableOrExpression(CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker someMarker)
	{
		PsiBuilder.Marker newMarker = builder.mark();

		int i = 0;
		PsiBuilder.Marker[] array = new PsiBuilder.Marker[2];
		while(!builder.eof())
		{
			if(i == 2)
			{
				break;
			}
			val expressionMarker = ExpressionParsing.parse(builder);
			if(expressionMarker == null)
			{
				break;
			}

			array[i++] = expressionMarker;
		}

		newMarker.rollbackTo();

		if((getElementType(array[0]) == REFERENCE_EXPRESSION || getElementType(array[0]) == ARRAY_ACCESS_EXPRESSION) && (getElementType(array[1])
				== REFERENCE_EXPRESSION || getElementType(array[1]) == ASSIGNMENT_EXPRESSION))
		{
			PsiBuilder.Marker varMarker = parseVariableDecl(builder, false);
			if(varMarker == null)
			{
				PsiBuilder.Marker expressionMarker = ExpressionParsing.parse(builder);
				if(expressionMarker == null)
				{
					builder.error("Expression expected");
					builder.advanceLexer();
				}
				else
				{
					if(someMarker != null)
					{
						expect(builder, SEMICOLON, "';' expected");
					}
				}

				if(someMarker != null)
				{
					someMarker.done(EXPRESSION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.EXPRESSION;
			}
			else
			{
				if(someMarker != null)
				{
					someMarker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.VARIABLE;
			}
		}
		else
		{
			PsiBuilder.Marker expressionMarker = ExpressionParsing.parse(builder);
			if(expressionMarker == null)
			{
				if(someMarker != null)
				{
					someMarker.drop();
				}
				return null;
			}
			else
			{
				if(someMarker != null)
				{
					expect(builder, SEMICOLON, "';' expected");
				}
			}

			if(someMarker != null)
			{
				someMarker.done(EXPRESSION_STATEMENT);
			}
			return ParseVariableOrExpressionResult.EXPRESSION;
		}
	}

	private static IElementType getElementType(PsiBuilder.Marker marker)
	{
		if(marker instanceof LighterASTNode)
		{
			return ((LighterASTNode) marker).getTokenType();
		}
		return null;
	}

	private static void parseTryStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker)
	{
		builder.advanceLexer();

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder);
		}
		else
		{
			builder.error("'{' expected");
		}

		boolean has = false;
		while(builder.getTokenType() == CATCH_KEYWORD)
		{
			parseCatchStatement(builder, null);
			has = true;
		}

		if(builder.getTokenType() == FINALLY_KEYWORD)
		{
			parseFinallyStatement(builder, null);
			has = true;
		}

		if(!has)
		{
			builder.error("'catch' or 'finally' expected");
		}
		marker.done(TRY_STATEMENT);
	}

	private static void parseCatchStatement(@NotNull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker marker)
	{
		PsiBuilder.Marker mark;
		if(marker != null)
		{
			builder.error("'try' expected");
			mark = marker;
		}
		else
		{
			mark = builder.mark();
		}

		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			builder.advanceLexer();

			PsiBuilder.Marker varMarker = builder.mark();

			if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
			{
				builder.error("Type expected");
				varMarker.drop();
			}
			else
			{
				expect(builder, IDENTIFIER, null);

				varMarker.done(LOCAL_VARIABLE);
			}

			expect(builder, RPAR, "')' expected");
		}

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder);
		}
		else
		{
			builder.error("'{' expected");
		}

		mark.done(CATCH_STATEMENT);
	}

	private static void parseFinallyStatement(@NotNull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker marker)
	{
		PsiBuilder.Marker mark;
		if(marker != null)
		{
			builder.error("'try' expected");
			mark = marker;
		}
		else
		{
			mark = builder.mark();
		}

		builder.advanceLexer();

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder);
		}
		else
		{
			builder.error("'{' expected");
		}

		mark.done(FINALLY_STATEMENT);
	}

	private static void parseLabeledStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker)
	{
		builder.advanceLexer();
		builder.advanceLexer();
		if(parseStatement(builder) == null)
		{
			builder.error("Statement expected");
		}
		marker.done(LABELED_STATEMENT);
	}

	private static void parseThrowStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker)
	{
		builder.advanceLexer();

		ExpressionParsing.parse(builder);

		expect(builder, SEMICOLON, "';' expected");
		marker.done(THROW_STATEMENT);
	}

	private static void parseForStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker)
	{
		builder.advanceLexer();
		if(expect(builder, LPAR, "'(' expected"))
		{
			if(builder.getTokenType() != SEMICOLON)
			{
				ParseVariableOrExpressionResult parseVariableOrExpressionResult = parseVariableOrExpression(builder, null);

				if(parseVariableOrExpressionResult == ParseVariableOrExpressionResult.EXPRESSION)
				{
					while(builder.getTokenType() == COMMA)
					{
						builder.advanceLexer();

						PsiBuilder.Marker nextMarker = ExpressionParsing.parse(builder);
						if(nextMarker == null)
						{
							builder.error("Expression expected");
						}
					}
				}
			}
			else
			{
				builder.advanceLexer();
			}

			if(builder.getTokenType() != SEMICOLON)
			{
				ExpressionParsing.parse(builder);
			}
			else
			{
				builder.advanceLexer();

				ExpressionParsing.parse(builder);
			}

			if(builder.getTokenType() != SEMICOLON)
			{
				ExpressionParsing.parse(builder);
			}
			else
			{
				builder.advanceLexer();

				ExpressionParsing.parse(builder);

				while(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();

					ExpressionParsing.parse(builder);
				}
			}

			expect(builder, RPAR, "')' expected");
		}

		parseStatement(builder);

		marker.done(FOR_STATEMENT);
	}

	private static void parseSwitch(@NotNull CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		builder.advanceLexer();

		if(!parseExpressionInParenth(builder))
		{
			builder.error("Expression expected");
		}

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder);
		}
		else
		{
			builder.error("'{' expected");
		}
		marker.done(SWITCH_STATEMENT);
	}

	private static void parseSwitchLabel(@NotNull CSharpBuilderWrapper builder, PsiBuilder.Marker marker, boolean caseLabel)
	{
		builder.advanceLexer();

		if(caseLabel)
		{
			if(ExpressionParsing.parse(builder) == null)
			{
				builder.error("Expression expected");
			}
		}

		expect(builder, COLON, "':' expected");

		marker.done(SWITCH_LABEL_STATEMENT);
	}

	private static void parseUsingOrFixed(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, IElementType to)
	{
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithRollback(builder, builder.mark(), LOCAL_VARIABLE) == null)
			{
				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}

			expect(builder, RPAR, "')' expected");
		}

		if(parseStatement(builder) == null)
		{
			builder.error("Statement expected");
		}

		marker.done(to);
	}

	@NotNull
	private static PsiBuilder.Marker parseIfStatement(final CSharpBuilderWrapper builder, final PsiBuilder.Marker mark)
	{
		builder.advanceLexer();

		if(!parseExpressionInParenth(builder))
		{
			mark.done(IF_STATEMENT);
			return mark;
		}

		val thenStatement = parseStatement(builder);
		if(thenStatement == null)
		{
			builder.error("Expected statement");
			mark.done(IF_STATEMENT);
			return mark;
		}

		if(!expect(builder, ELSE_KEYWORD, null))
		{
			mark.done(IF_STATEMENT);
			return mark;
		}

		val elseStatement = parseStatement(builder);
		if(elseStatement == null)
		{
			builder.error("Expected statement");
		}

		mark.done(IF_STATEMENT);
		return mark;
	}

	private static void parseForeach(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		assert builder.getTokenType() == FOREACH_KEYWORD;

		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			PsiBuilder.Marker varMarker = builder.mark();

			if(parseType(builder, BracketFailPolicy.NOTHING, true) != null)
			{
				expect(builder, IDENTIFIER, "Identifier expected");
			}
			else
			{
				builder.error("Type expected");
			}

			varMarker.done(LOCAL_VARIABLE);

			if(expect(builder, IN_KEYWORD, "'in' expected"))
			{
				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}

			expect(builder, RPAR, "')' expected");
		}

		if(builder.getTokenType() == SEMICOLON)
		{
			builder.advanceLexer();
		}
		else
		{
			StatementParsing.parse(builder);
		}

		marker.done(FOREACH_STATEMENT);
	}

	private static PsiBuilder.Marker parseBlockStatement(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		if(builder.getTokenType() == LBRACE)
		{
			builder.advanceLexer();

			while(!builder.eof())
			{
				if(builder.getTokenType() == RBRACE)
				{
					break;
				}
				else
				{
					PsiBuilder.Marker anotherMarker = parse(builder);
					if(anotherMarker == null)
					{
						break;
					}
				}
			}

			expect(builder, RBRACE, "'}' expected");
			marker.done(BLOCK_STATEMENT);
			return marker;
		}
		else
		{
			builder.error("'{' expected");
			return null;
		}
	}

	private static boolean parseExpressionInParenth(final CSharpBuilderWrapper builder)
	{
		if(!expect(builder, LPAR, "'(' expected"))
		{
			return false;
		}

		final PsiBuilder.Marker beforeExpr = builder.mark();
		final PsiBuilder.Marker expr = ExpressionParsing.parse(builder);
		if(expr == null || builder.getTokenType() == SEMICOLON)
		{
			beforeExpr.rollbackTo();
			builder.error("Expression expected");
			if(builder.getTokenType() != RPAR)
			{
				return false;
			}
		}
		else
		{
			beforeExpr.drop();
			if(builder.getTokenType() != RPAR)
			{
				builder.error("')' expected");
				return false;
			}
		}

		builder.advanceLexer();
		return true;
	}

	private static void parseYieldStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker)
	{
		assert wrapper.getTokenType() == YIELD_KEYWORD;

		wrapper.advanceLexer();

		if(wrapper.getTokenType() == BREAK_KEYWORD)
		{
			PsiBuilder.Marker mark = wrapper.mark();
			wrapper.advanceLexer();
			mark.done(BREAK_STATEMENT);
		}
		else if(wrapper.getTokenType() == RETURN_KEYWORD)
		{
			PsiBuilder.Marker mark = wrapper.mark();
			wrapper.advanceLexer();
			ExpressionParsing.parse(wrapper);
			mark.done(RETURN_STATEMENT);
		}
		else
		{
			wrapper.error("'break' or 'return' expected");
		}

		expect(wrapper, SEMICOLON, "';' expected");

		marker.done(YIELD_STATEMENT);
	}

	private static void parseReturnStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker)
	{
		assert wrapper.getTokenType() == RETURN_KEYWORD;

		wrapper.advanceLexer();

		ExpressionParsing.parse(wrapper);

		expect(wrapper, SEMICOLON, "';' expected");

		marker.done(RETURN_STATEMENT);
	}

	private static void parseDoWhileStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker)
	{
		wrapper.advanceLexer();

		if(wrapper.getTokenType() == LBRACE)
		{
			parseStatement(wrapper);
		}
		else
		{
			wrapper.error("'{' expected");
		}

		if(expect(wrapper, WHILE_KEYWORD, "'while' expected"))
		{
			parseExpressionInParenth(wrapper);

			expect(wrapper, SEMICOLON, null);
		}

		marker.done(DO_WHILE_STATEMENT);
	}

	private static void parseCheckedStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker)
	{
		if(wrapper.lookAhead(1) == LPAR)
		{
			ExpressionParsing.parse(wrapper);

			marker.done(EXPRESSION_STATEMENT);
		}
		else
		{
			wrapper.advanceLexer();

			if(wrapper.getTokenType() == LBRACE)
			{
				parseStatement(wrapper);
			}
			else
			{
				wrapper.error("'{' expected");
			}

			marker.done(CHECKED_STATEMENT);
		}
	}

	private static void parseStatementWithParenthesesExpression(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker, IElementType doneElement)
	{
		wrapper.advanceLexer();

		parseExpressionInParenth(wrapper);

		StatementParsing.parse(wrapper);

		expect(wrapper, SEMICOLON, null);

		marker.done(doneElement);
	}

	private static PsiBuilder.Marker parseVariableDecl(CSharpBuilderWrapper builder, boolean constToken)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(constToken)
		{
			builder.advanceLexer();
		}

		TypeInfo typeMarker = parseType(builder, BracketFailPolicy.DROP, true);
		if(typeMarker == null)
		{
			if(constToken)
			{
				builder.error("Type expected");
			}
			mark.rollbackTo();

			return null;
		}

		if(builder.getTokenType() == IDENTIFIER)
		{
			builder.advanceLexer();

			if(builder.getTokenType() == EQ)
			{
				builder.advanceLexer();
				PsiBuilder.Marker parse = ExpressionParsing.parse(builder);
				if(parse == null)
				{
					builder.error("Expression expected");
				}
			}

			if(builder.getTokenType() == COMMA)
			{
				mark.done(LOCAL_VARIABLE);

				builder.advanceLexer();

				FieldOrPropertyParsing.parseFieldOrLocalVariableAtNameWithDone(builder, builder.mark(), LOCAL_VARIABLE);

				expect(builder, SEMICOLON, "';' expected");
			}
			else
			{
				expect(builder, SEMICOLON, "';' expected");

				mark.done(LOCAL_VARIABLE);
			}
			return mark;
		}

		mark.rollbackTo();
		return null;
	}
}
