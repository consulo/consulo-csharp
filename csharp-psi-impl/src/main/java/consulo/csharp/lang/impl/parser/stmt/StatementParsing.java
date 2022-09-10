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

package consulo.csharp.lang.impl.parser.stmt;

import consulo.language.parser.PsiBuilder;
import consulo.util.lang.Pair;
import consulo.util.lang.StringUtil;
import consulo.language.ast.IElementType;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.decl.FieldOrPropertyParsing;
import consulo.csharp.lang.impl.parser.decl.GenericParameterParsing;
import consulo.csharp.lang.impl.parser.decl.MethodParsing;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.LighterASTNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	private static PsiBuilder.Marker parseStatement(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker marker = builder.mark();

		builder.enableSoftKeyword(CSharpSoftTokens.YIELD_KEYWORD);
		builder.enableSoftKeyword(CSharpSoftTokens.ASYNC_KEYWORD);

		IElementType tokenType = builder.getTokenType();

		builder.disableSoftKeyword(CSharpSoftTokens.YIELD_KEYWORD);
		builder.disableSoftKeyword(CSharpSoftTokens.ASYNC_KEYWORD);

		if(tokenType == LOCK_KEYWORD)
		{
			parseStatementWithParenthesesExpression(builder, marker, LOCK_STATEMENT, set);
		}
		else if(tokenType == BREAK_KEYWORD)
		{
			builder.advanceLexer();

			expect(builder, SEMICOLON, "';' expected");

			marker.done(BREAK_STATEMENT);
		}
		else if(tokenType == GOTO_KEYWORD)
		{
			builder.advanceLexer();

			final IElementType nextTokenType = builder.getTokenType();
			if(nextTokenType == CSharpTokens.CASE_KEYWORD)
			{
				builder.advanceLexer();

				if(ExpressionParsing.parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
			}
			else if(nextTokenType == CSharpTokens.DEFAULT_KEYWORD)
			{
				builder.advanceLexer();
			}
			else
			{
				doneOneElement(builder, CSharpTokens.IDENTIFIER, REFERENCE_EXPRESSION, "Identifier expected");
			}

			expect(builder, SEMICOLON, "';' expected");

			marker.done(GOTO_STATEMENT);
		}
		else if(tokenType == CONTINUE_KEYWORD)
		{
			builder.advanceLexer();

			expect(builder, SEMICOLON, "';' expected");

			marker.done(CONTINUE_STATEMENT);
		}
		else if(tokenType == FOR_KEYWORD)
		{
			parseForStatement(builder, marker, set);
		}
		else if(tokenType == DO_KEYWORD)
		{
			parseDoWhileStatement(builder, marker, set);
		}
		else if(tokenType == RETURN_KEYWORD)
		{
			parseReturnStatement(builder, marker, set);
		}
		else if(tokenType == THROW_KEYWORD)
		{
			parseThrowStatement(builder, marker, set);
		}
		else if(tokenType == FOREACH_KEYWORD)
		{
			parseForeachStatement(builder, marker, set);
		}
		else if(tokenType == YIELD_KEYWORD)
		{
			parseYieldStatement(builder, marker, set);
		}
		else if(tokenType == TRY_KEYWORD)
		{
			parseTryStatement(builder, marker, set);
		}
		else if(tokenType == CATCH_KEYWORD)
		{
			parseCatchStatement(builder, marker, set);
		}
		else if(tokenType == FINALLY_KEYWORD)
		{
			parseFinallyStatement(builder, marker, set);
		}
		else if(tokenType == ASYNC_KEYWORD)
		{
			PsiBuilder.Marker methodMarker = parseLocalMethodDeclaration(builder, marker, false);
			if(methodMarker != null)
			{
				return methodMarker;
			}

			builder.remapBackIfSoft();

			return parseLabelOrVariableOrExpression(tokenType, builder, marker, set);
		}
		else if(tokenType == UNSAFE_KEYWORD)
		{
			PsiBuilder.Marker methodMarker = parseLocalMethodDeclaration(builder, marker, false);
			if(methodMarker != null)
			{
				return methodMarker;
			}
			parseUnsafeStatement(builder, marker, set);
		}
		else if(tokenType == IF_KEYWORD)
		{
			parseIfStatement(builder, marker, set);
		}
		else if(tokenType == LBRACE)
		{
			parseBlockStatement(builder, marker, set);
		}
		else if(tokenType == WHILE_KEYWORD)
		{
			parseStatementWithParenthesesExpression(builder, marker, WHILE_STATEMENT, set);
		}
		else if(tokenType == CHECKED_KEYWORD || tokenType == UNCHECKED_KEYWORD)
		{
			parseCheckedStatement(builder, marker, set);
		}
		else if(tokenType == USING_KEYWORD)
		{
			parseUsingOrFixed(builder, marker, USING_STATEMENT, set);
		}
		else if(tokenType == FIXED_KEYWORD)
		{
			parseUsingOrFixed(builder, marker, FIXED_STATEMENT, set);
		}
		else if(tokenType == CASE_KEYWORD || tokenType == DEFAULT_KEYWORD && builder.lookAhead(1) == COLON)  // accept with colon only,
		// default can be expression
		{
			parseSwitchLabel(builder, marker, tokenType == CASE_KEYWORD, set);
		}
		else if(tokenType == SWITCH_KEYWORD)
		{
			parseSwitchStatement(builder, marker, set);
		}
		else if(tokenType == CONST_KEYWORD)
		{
			PsiBuilder.Marker varMark = builder.mark();

			builder.advanceLexer();

			FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(builder, varMark, LOCAL_VARIABLE, NONE, true, set);

			marker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
		}
		else if(tokenType == SEMICOLON)
		{
			builder.advanceLexer();
			marker.done(EMPTY_STATEMENT);
		}
		else
		{
			return parseLabelOrVariableOrExpression(tokenType, builder, marker, set);
		}
		return marker;
	}

	@Nullable
	private static PsiBuilder.Marker parseLabelOrVariableOrExpression(IElementType tokenType, CSharpBuilderWrapper builder, @Nonnull PsiBuilder.Marker marker, ModifierSet set)
	{
		if(tokenType == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == COLON)
		{
			parseLabeledStatement(builder, marker, set);
			return marker;
		}

		if(parseVariableOrExpression(builder, marker, set) == null)
		{
			return null;
		}

		return marker;
	}

	@Nullable
	private static PsiBuilder.Marker parseLocalMethodDeclaration(CSharpBuilderWrapper builder, @Nonnull PsiBuilder.Marker statementMarker, boolean rollbackBody)
	{
		PsiBuilder.Marker methodMarker = builder.mark();

		ModifierSet modifierSet = ModifierSet.EMPTY;

		IElementType tokenType = builder.getTokenType();
		if(tokenType == UNSAFE_KEYWORD || tokenType == ASYNC_KEYWORD)
		{
			Pair<PsiBuilder.Marker, ModifierSet> pair = parseModifierList(builder, NONE);
			modifierSet = pair.getSecond();
		}

		TypeInfo type = parseType(builder, NONE);
		if(type == null)
		{
			methodMarker.rollbackTo();
			return null;
		}

		// name (
		IElementType nextElement = builder.lookAhead(1);
		if(builder.getTokenType() == CSharpTokens.IDENTIFIER && (nextElement == LPAR || nextElement == LT))
		{
			doneIdentifier(builder, NONE);

			if(nextElement == LT)
			{
				GenericParameterParsing.parseList(builder);
			}

			MethodParsing.parseParameterList(builder, SharedParsingHelpers.NONE, RPAR, modifierSet);

			if(!MethodParsing.parseMethodBody(builder, modifierSet))
			{
				if(rollbackBody)
				{
					methodMarker.rollbackTo();
					return null;
				}
				else
				{
					builder.error("Expected body");
				}
			}

			methodMarker.done(LOCAL_METHOD);

			statementMarker.done(LOCAL_METHOD_STATEMENT);

			return statementMarker;
		}
		else
		{
			methodMarker.rollbackTo();
			return null;
		}
	}

	private static enum ParseVariableOrExpressionResult
	{
		VARIABLE,
		METHOD,
		EXPRESSION
	}

	@Nullable
	private static ParseVariableOrExpressionResult parseVariableOrExpression(CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker statementMarker, ModifierSet set)
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
					if(statementMarker != null)
					{
						statementMarker.drop();
					}
					return null;
				}
				else
				{
					if(statementMarker != null)
					{
						expect(builder, SEMICOLON, "';' expected");
					}
				}

				if(statementMarker != null)
				{
					statementMarker.done(EXPRESSION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.EXPRESSION;
			case WITH_NAME:
				PsiBuilder.Marker mark = builder.mark();
				FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(builder, mark, LOCAL_VARIABLE, VAR_SUPPORT, false, set);
				if(statementMarker != null)
				{
					expect(builder, SEMICOLON, "';' expected");
					statementMarker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.VARIABLE;
			case LOCAL_METHOD:
				PsiBuilder.Marker marker = parseLocalMethodDeclaration(builder, statementMarker == null ? builder.mark() : statementMarker, false);
				if(marker == null)
				{
					return null;
				}
				return ParseVariableOrExpressionResult.METHOD;
			case NO_NAME:
				PsiBuilder.Marker newMarker = builder.mark();
				TypeInfo typeInfo = parseType(builder, BRACKET_RETURN_BEFORE | VAR_SUPPORT);
				assert typeInfo != null;
				reportIdentifier(builder, NONE);
				newMarker.done(LOCAL_VARIABLE);

				if(statementMarker != null)
				{
					expect(builder, SEMICOLON, "';' expected");
					statementMarker.done(LOCAL_VARIABLE_DECLARATION_STATEMENT);
				}
				return ParseVariableOrExpressionResult.VARIABLE;
			case TYPED_DECONSTRUCTION:
				parseType(builder);

				if(builder.getTokenType() == EQ)
				{
					builder.advanceLexer();

					if(ExpressionParsing.parse(builder, ModifierSet.EMPTY) == null)
					{
						builder.error("Expression expected");
					}
				}

				if(statementMarker != null)
				{
					expect(builder, SEMICOLON, "';' expected");

					statementMarker.done(DECONSTRUCTION_STATEMENT);
				}
				else
				{
					builder.error("Statement expected");
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
		LOCAL_METHOD,
		NO_NAME,
		TYPED_DECONSTRUCTION
	}

	@Nonnull
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

				IElementType nextElement = builder.lookAhead(1);
				if(nextElement == LPAR || nextElement == LT)
				{
					return LocalVarType.LOCAL_METHOD;
				}

				if(StringUtil.containsLineBreak(sequence))
				{
					return LocalVarType.NO_NAME;
				}
				return LocalVarType.WITH_NAME;
			}
			else
			{
				if(typeInfo.isTuple && builder.getTokenType() == CSharpTokens.EQ)
				{
					return LocalVarType.TYPED_DECONSTRUCTION;
				}

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

	private static void parseTryStatement(@Nonnull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet set)
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

	private static void parseCatchStatement(@Nonnull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker marker, ModifierSet set)
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

	private static void parseFinallyStatement(@Nonnull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker marker, ModifierSet set)
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

	private static void parseUnsafeStatement(@Nonnull CSharpBuilderWrapper builder, @Nonnull PsiBuilder.Marker marker, ModifierSet set)
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

	private static void parseLabeledStatement(@Nonnull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet set)
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

	private static void parseThrowStatement(@Nonnull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet set)
	{
		builder.advanceLexer();

		ExpressionParsing.parse(builder, set);

		expect(builder, SEMICOLON, "';' expected");
		marker.done(THROW_STATEMENT);
	}

	private static void parseForStatement(@Nonnull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, ModifierSet modifierSet)
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

			expect(builder, CSharpTokens.SEMICOLON, "';' expected");
			ExpressionParsing.parse(builder, modifierSet);

			expect(builder, CSharpTokens.SEMICOLON, "';' expected");
			ExpressionParsing.parse(builder, modifierSet);

			while(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();

				ExpressionParsing.parse(builder, modifierSet);
			}

			expect(builder, RPAR, "')' expected");
		}

		if(parseStatement(builder, modifierSet) == null)
		{
			builder.error("Statement expected");
		}

		marker.done(FOR_STATEMENT);
	}

	private static void parseSwitchStatement(@Nonnull CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
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

	private static void parseSwitchLabel(@Nonnull CSharpBuilderWrapper builder, PsiBuilder.Marker marker, boolean caseLabel, ModifierSet set)
	{
		builder.advanceLexer();

		IElementType doneElement = CASE_OR_DEFAULT_STATEMENT;

		if(caseLabel)
		{
			if(!parseCasePatternStatement(builder, set))
			{
				if(ExpressionParsing.parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
			}
			else
			{
				doneElement = CASE_PATTERN_STATEMENT;
			}
		}

		expect(builder, COLON, "':' expected");

		marker.done(doneElement);
	}

	private static boolean parseCasePatternStatement(@Nonnull CSharpBuilderWrapper builder, @Nonnull ModifierSet set)
	{
		PsiBuilder.Marker patternVarMarker = builder.mark();

		TypeInfo typeInfo = parseType(builder, UNEXPECTED_TUPLE);
		if(typeInfo == null)
		{
			patternVarMarker.rollbackTo();
			return false;
		}

		if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
		{
			doneIdentifier(builder, NONE);

			patternVarMarker.done(CASE_VARIABLE);

			builder.enableSoftKeyword(CSharpSoftTokens.WHEN_KEYWORD);

			IElementType possibleWhenTokenType = builder.getTokenType();

			builder.disableSoftKeyword(CSharpSoftTokens.WHEN_KEYWORD);

			if(possibleWhenTokenType == CSharpSoftTokens.WHEN_KEYWORD)
			{
				builder.advanceLexer();

				if(ExpressionParsing.parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
			}
			else if(possibleWhenTokenType != COLON)
			{
				builder.error("'when' expected");
			}

			return true;
		}
		else
		{
			patternVarMarker.rollbackTo();
			return false;
		}
	}

	private static void parseUsingOrFixed(@Nonnull CSharpBuilderWrapper builder, final PsiBuilder.Marker marker, IElementType to, ModifierSet set)
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

	@Nonnull
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
