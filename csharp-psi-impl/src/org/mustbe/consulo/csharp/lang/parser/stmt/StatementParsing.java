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
import org.mustbe.consulo.csharp.lang.parser.ModifierSet;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.decl.FieldOrPropertyParsing;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 16.12.13.
 */
public class StatementParsing extends SharedParsingHelpers
{
	private static final String AWAIT_KEYWORD = "await";

	public static PsiBuilder.Marker parse(CSharpBuilderWrapper wrapper, ModifierSet set)
	{
		return parseStatement(wrapper, set);
	}

	private static PsiBuilder.Marker parseStatement(CSharpBuilderWrapper wrapper, ModifierSet set)
	{
		PsiBuilder.Marker marker = wrapper.mark();

		wrapper.enableSoftKeyword(CSharpSoftTokens.YIELD_KEYWORD);

		IElementType tokenType = wrapper.getTokenType();

		wrapper.disableSoftKeyword(CSharpSoftTokens.YIELD_KEYWORD);

		if(tokenType == LOCK_KEYWORD)
		{
			parseStatementWithParenthesesExpression(wrapper, marker, LOCK_STATEMENT, set);
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

			doneOneElement(wrapper, CSharpTokens.IDENTIFIER, REFERENCE_EXPRESSION, "Identifier expected");

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
			parseForStatement(wrapper, marker, set);
		}
		else if(tokenType == DO_KEYWORD)
		{
			parseDoWhileStatement(wrapper, marker, set);
		}
		else if(tokenType == RETURN_KEYWORD)
		{
			parseReturnStatement(wrapper, marker, set);
		}
		else if(tokenType == THROW_KEYWORD)
		{
			parseThrowStatement(wrapper, marker, set);
		}
		else if(tokenType == FOREACH_KEYWORD)
		{
			parseForeachStatement(wrapper, marker, set);
		}
		else if(tokenType == YIELD_KEYWORD)
		{
			parseYieldStatement(wrapper, marker, set);
		}
		else if(tokenType == TRY_KEYWORD)
		{
			parseTryStatement(wrapper, marker, set);
		}
		else if(tokenType == CATCH_KEYWORD)
		{
			parseCatchStatement(wrapper, marker, set);
		}
		else if(tokenType == FINALLY_KEYWORD)
		{
			parseFinallyStatement(wrapper, marker, set);
		}
		else if(tokenType == UNSAFE_KEYWORD)
		{
			parseUnsafeStatement(wrapper, marker, set);
		}
		else if(tokenType == IF_KEYWORD)
		{
			parseIfStatement(wrapper, marker, set);
		}
		else if(tokenType == LBRACE)
		{
			parseBlockStatement(wrapper, marker, set);
		}
		else if(tokenType == WHILE_KEYWORD)
		{
			parseStatementWithParenthesesExpression(wrapper, marker, WHILE_STATEMENT, set);
		}
		else if(tokenType == CHECKED_KEYWORD || tokenType == UNCHECKED_KEYWORD)
		{
			parseCheckedStatement(wrapper, marker, set);
		}
		else if(tokenType == USING_KEYWORD)
		{
			parseUsingOrFixed(wrapper, marker, USING_STATEMENT, set);
		}
		else if(tokenType == FIXED_KEYWORD)
		{
			parseUsingOrFixed(wrapper, marker, FIXED_STATEMENT, set);
		}
		else if(tokenType == CASE_KEYWORD || tokenType == DEFAULT_KEYWORD && wrapper.lookAhead(1) == COLON)  // accept with colon only,
		// default can be expression
		{
			parseSwitchLabel(wrapper, marker, tokenType == CASE_KEYWORD, set);
		}
		else if(tokenType == SWITCH_KEYWORD)
		{
			parseSwitchStatement(wrapper, marker, set);
		}
		else if(tokenType == CONST_KEYWORD)
		{
			PsiBuilder.Marker varMark = wrapper.mark();

			wrapper.advanceLexer();

			FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(wrapper, varMark, LOCAL_VARIABLE, NONE, true, set);

			marker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
		}
		else if(tokenType == SEMICOLON)
		{
			wrapper.advanceLexer();
			marker.done(EMPTY_STATEMENT);
		}
		else
		{
			if(tokenType == CSharpTokens.IDENTIFIER && wrapper.lookAhead(1) == COLON)
			{
				parseLabeledStatement(wrapper, marker, set);
				return marker;
			}

			if(parseVariableOrExpression(wrapper, marker, set) == null)
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
	private static ParseVariableOrExpressionResult parseVariableOrExpression(CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker someMarker, ModifierSet set)
	{
		LocalVarType localVarType = canParseAsVariable(builder, set);
		// need for example remap global keyword to identifier when it try to parse
		builder.remapBackIfSoft();

		switch(localVarType)
		{

			case NONE:
				PsiBuilder.Marker expressionMarker = ExpressionParsing.parse(builder, set);
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
			case WITH_NAME:
				PsiBuilder.Marker mark = builder.mark();
				FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(builder, mark, LOCAL_VARIABLE, VAR_SUPPORT, false, set);
				if(someMarker != null)
				{
					expect(builder, SEMICOLON, "';' expected");
					someMarker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.VARIABLE;
			case NO_NAME:
				PsiBuilder.Marker newMarker = builder.mark();
				TypeInfo typeInfo = parseType(builder, BRACKET_RETURN_BEFORE | VAR_SUPPORT);
				assert typeInfo != null;
				reportIdentifier(builder, NONE);
				newMarker.done(LOCAL_VARIABLE);

				if(someMarker != null)
				{
					expect(builder, SEMICOLON, "';' expected");
					someMarker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.VARIABLE;
			default:
				throw new UnsupportedOperationException();
		}
	}

	enum LocalVarType
	{
		NONE,
		WITH_NAME,
		NO_NAME
	}

	@NotNull
	private static LocalVarType canParseAsVariable(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker newMarker = builder.mark();

		try
		{
			int currentOffset = builder.getCurrentOffset();
			TypeInfo typeInfo = parseType(builder, BRACKET_RETURN_BEFORE | VAR_SUPPORT);
			if(typeInfo == null)
			{
				return LocalVarType.NONE;
			}

			if(set.contains(CSharpSoftTokens.ASYNC_KEYWORD))
			{
				int startOffset = ((LighterASTNode) typeInfo.marker).getStartOffset();
				int endOffset = ((LighterASTNode) typeInfo.marker).getEndOffset();

				CharSequence sequence = builder.getOriginalText().subSequence(startOffset, endOffset);

				if(isAwait(sequence))
				{
					return LocalVarType.NONE;
				}
			}

			IElementType tokenType = builder.getTokenType();
			if(tokenType == LPAR)
			{
				return LocalVarType.NONE;
			}

			CharSequence sequence = builder.getOriginalText().subSequence(currentOffset, builder.getCurrentOffset());
			if(tokenType == CSharpTokens.IDENTIFIER)
			{
				if(builder.lookAhead(1) == CSharpTokens.SEMICOLON || builder.lookAhead(1) == CSharpTokens.EQ)
				{
					return LocalVarType.WITH_NAME;
				}

				if(StringUtil.containsLineBreak(sequence))
				{
					return LocalVarType.NO_NAME;
				}
				return LocalVarType.WITH_NAME;
			}
			else
			{
				if(StringUtil.containsLineBreak(sequence))
				{
					return LocalVarType.NO_NAME;
				}
			}
			return LocalVarType.NONE;
		}
		finally
		{
			newMarker.rollbackTo();
		}
	}

	private static boolean isAwait(CharSequence sequence)
	{
		if(sequence.length() < AWAIT_KEYWORD.length())
		{
			return false;
		}

		// check for await
		for(int i = 0; i < AWAIT_KEYWORD.length(); i++)
		{
			char expectedChar = AWAIT_KEYWORD.charAt(i);
			char actualChar = sequence.charAt(i);
			if(actualChar != expectedChar)
			{
				return false;
			}
		}

		if(sequence.length() == AWAIT_KEYWORD.length())
		{
			return true;
		}

		for(int i = AWAIT_KEYWORD.length(); i < sequence.length(); i++)
		{
			char c = sequence.charAt(i);
			if(!StringUtil.isWhiteSpace(c))
			{
				return false;
			}
		}
		return true;
	}

	private static void parseTryStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet set)
	{
		builder.advanceLexer();

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder, set);
		}
		else
		{
			builder.error("'{' expected");
		}

		boolean has = false;
		while(builder.getTokenType() == CATCH_KEYWORD)
		{
			parseCatchStatement(builder, null, set);
			has = true;
		}

		if(builder.getTokenType() == FINALLY_KEYWORD)
		{
			parseFinallyStatement(builder, null, set);
			has = true;
		}

		if(!has)
		{
			builder.error("'catch' or 'finally' expected");
		}
		marker.done(TRY_STATEMENT);
	}

	private static void parseCatchStatement(@NotNull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker marker, ModifierSet set)
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

			if(parseType(builder) == null)
			{
				builder.error("Type expected");
				varMarker.drop();
			}
			else
			{
				if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
				{
					doneIdentifier(builder, 0);
				}

				varMarker.done(LOCAL_VARIABLE);
			}

			expect(builder, RPAR, "')' expected");
		}

		builder.enableSoftKeyword(CSharpSoftTokens.WHEN_KEYWORD);
		IElementType tokenType = builder.getTokenType();
		builder.disableSoftKeyword(CSharpSoftTokens.WHEN_KEYWORD);

		if(tokenType == CSharpSoftTokens.WHEN_KEYWORD)
		{
			builder.advanceLexer();

			parseExpressionInParenth(builder, set);
		}

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder, set);
		}
		else
		{
			builder.error("'{' expected");
		}

		mark.done(CATCH_STATEMENT);
	}

	private static void parseFinallyStatement(@NotNull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker marker, ModifierSet set)
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
			parseStatement(builder, set);
		}
		else
		{
			builder.error("'{' expected");
		}

		mark.done(FINALLY_STATEMENT);
	}

	private static void parseUnsafeStatement(@NotNull CSharpBuilderWrapper builder, @NotNull PsiBuilder.Marker marker, ModifierSet set)
	{
		builder.advanceLexer();

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder, set);
		}
		else
		{
			builder.error("'{' expected");
		}

		marker.done(UNSAFE_STATEMENT);
	}

	private static void parseLabeledStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet set)
	{
		builder.advanceLexer();
		builder.advanceLexer();

		boolean empty = true;

		while(parseStatement(builder, set) != null)
		{
			empty = false;
		}

		if(empty)
		{
			builder.error("Expected statement");
		}
		marker.done(LABELED_STATEMENT);
	}

	private static void parseThrowStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet set)
	{
		builder.advanceLexer();

		ExpressionParsing.parse(builder, set);

		expect(builder, SEMICOLON, "';' expected");
		marker.done(THROW_STATEMENT);
	}

	private static void parseForStatement(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet modifierSet)
	{
		builder.advanceLexer();
		if(expect(builder, LPAR, "'(' expected"))
		{
			if(builder.getTokenType() != SEMICOLON)
			{
				ParseVariableOrExpressionResult parseVariableOrExpressionResult = parseVariableOrExpression(builder, null, modifierSet);

				if(parseVariableOrExpressionResult == ParseVariableOrExpressionResult.EXPRESSION)
				{
					while(builder.getTokenType() == COMMA)
					{
						builder.advanceLexer();

						PsiBuilder.Marker nextMarker = ExpressionParsing.parse(builder, modifierSet);
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
				ExpressionParsing.parse(builder, modifierSet);
			}
			else
			{
				builder.advanceLexer();

				ExpressionParsing.parse(builder, modifierSet);
			}

			if(builder.getTokenType() != SEMICOLON)
			{
				ExpressionParsing.parse(builder, modifierSet);
			}
			else
			{
				builder.advanceLexer();

				ExpressionParsing.parse(builder, modifierSet);

				while(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();

					ExpressionParsing.parse(builder, modifierSet);
				}
			}

			expect(builder, RPAR, "')' expected");
		}

		if(parseStatement(builder, modifierSet) == null)
		{
			builder.error("Statement expected");
		}

		marker.done(FOR_STATEMENT);
	}

	private static void parseSwitchStatement(@NotNull CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
	{
		builder.advanceLexer();

		if(!parseExpressionInParenth(builder, set))
		{
			builder.error("Expression expected");
		}

		if(builder.getTokenType() == LBRACE)
		{
			parseStatement(builder, set);
		}
		else
		{
			builder.error("'{' expected");
		}
		marker.done(SWITCH_STATEMENT);
	}

	private static void parseSwitchLabel(@NotNull CSharpBuilderWrapper builder, PsiBuilder.Marker marker, boolean caseLabel, ModifierSet set)
	{
		builder.advanceLexer();

		if(caseLabel)
		{
			if(ExpressionParsing.parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
		}

		expect(builder, COLON, "':' expected");

		marker.done(SWITCH_LABEL_STATEMENT);
	}

	private static void parseUsingOrFixed(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, IElementType to, ModifierSet set)
	{
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parseVariableOrExpression(builder, null, set) == null)
			{
				builder.error("Variable or expression expected");
			}

			expect(builder, RPAR, "')' expected");
		}

		if(parseStatement(builder, set) == null)
		{
			builder.error("Statement expected");
		}

		marker.done(to);
	}

	@NotNull
	private static PsiBuilder.Marker parseIfStatement(final CSharpBuilderWrapper builder, final PsiBuilder.Marker mark, ModifierSet set)
	{
		builder.advanceLexer();

		if(!parseExpressionInParenth(builder, set))
		{
			mark.done(IF_STATEMENT);
			return mark;
		}

		PsiBuilder.Marker thenStatement = parseStatement(builder, set);
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

		PsiBuilder.Marker elseStatement = parseStatement(builder, set);
		if(elseStatement == null)
		{
			builder.error("Expected statement");
		}

		mark.done(IF_STATEMENT);
		return mark;
	}

	private static void parseForeachStatement(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
	{
		assert builder.getTokenType() == FOREACH_KEYWORD;

		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			PsiBuilder.Marker varMarker = builder.mark();

			if(parseType(builder, VAR_SUPPORT) != null)
			{
				expectOrReportIdentifier(builder, 0);
			}
			else
			{
				builder.error("Type expected");
			}

			varMarker.done(LOCAL_VARIABLE);

			if(expect(builder, IN_KEYWORD, "'in' expected"))
			{
				if(ExpressionParsing.parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
			}

			expect(builder, RPAR, "')' expected");
		}

		if(parseStatement(builder, set) == null)
		{
			builder.error("Statement expected");
		}

		marker.done(FOREACH_STATEMENT);
	}

	private static PsiBuilder.Marker parseBlockStatement(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
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
					PsiBuilder.Marker anotherMarker = parse(builder, set);
					if(anotherMarker == null)
					{
						PsiBuilder.Marker mark = builder.mark();
						builder.advanceLexer();
						mark.error("Unexpected token");
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

	private static boolean parseExpressionInParenth(final CSharpBuilderWrapper builder, ModifierSet set)
	{
		if(!expect(builder, LPAR, "'(' expected"))
		{
			return false;
		}

		final PsiBuilder.Marker beforeExpr = builder.mark();
		final PsiBuilder.Marker expr = ExpressionParsing.parse(builder, set);
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

	private static void parseYieldStatement(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
	{
		assert builder.getTokenType() == YIELD_KEYWORD;

		builder.advanceLexer();

		if(builder.getTokenType() == BREAK_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			mark.done(BREAK_STATEMENT);
		}
		else if(builder.getTokenType() == RETURN_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			ExpressionParsing.parse(builder, set);
			mark.done(RETURN_STATEMENT);
		}
		else
		{
			PsiBuilder.Marker mark = builder.mark();
			if(builder.getTokenType() != RBRACE && builder.getTokenType() != SEMICOLON)
			{
				builder.advanceLexer();
			}
			mark.error("'break' or 'return' expected");
		}

		expect(builder, SEMICOLON, "';' expected");

		marker.done(YIELD_STATEMENT);
	}

	private static void parseReturnStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker, ModifierSet set)
	{
		assert wrapper.getTokenType() == RETURN_KEYWORD;

		wrapper.advanceLexer();

		ExpressionParsing.parse(wrapper, set);

		expect(wrapper, SEMICOLON, "';' expected");

		marker.done(RETURN_STATEMENT);
	}

	private static void parseDoWhileStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker, ModifierSet set)
	{
		wrapper.advanceLexer();

		if(wrapper.getTokenType() == LBRACE)
		{
			parseStatement(wrapper, set);
		}
		else
		{
			wrapper.error("'{' expected");
		}

		if(expect(wrapper, WHILE_KEYWORD, "'while' expected"))
		{
			parseExpressionInParenth(wrapper, set);

			expect(wrapper, SEMICOLON, null);
		}

		marker.done(DO_WHILE_STATEMENT);
	}

	private static void parseCheckedStatement(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker, ModifierSet set)
	{
		if(wrapper.lookAhead(1) == LPAR)
		{
			ExpressionParsing.parse(wrapper, set);

			marker.done(EXPRESSION_STATEMENT);
		}
		else
		{
			wrapper.advanceLexer();

			if(wrapper.getTokenType() == LBRACE)
			{
				parseStatement(wrapper, set);
			}
			else
			{
				wrapper.error("'{' expected");
			}

			marker.done(CHECKED_STATEMENT);
		}
	}

	private static void parseStatementWithParenthesesExpression(CSharpBuilderWrapper wrapper, PsiBuilder.Marker marker, IElementType doneElement, ModifierSet set)
	{
		wrapper.advanceLexer();

		parseExpressionInParenth(wrapper, set);

		if(StatementParsing.parse(wrapper, set) == null)
		{
			wrapper.error("Statement expected");
		}

		marker.done(doneElement);
	}
}
