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

package consulo.csharp.lang.impl.parser.exp;

import consulo.language.parser.PsiBuilder;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.util.lang.BitUtil;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.decl.MethodParsing;
import consulo.csharp.lang.impl.parser.stmt.StatementParsing;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class ExpressionParsing extends SharedParsingHelpers
{
	private enum ExprType
	{
		CONDITIONAL_OR,
		CONDITIONAL_AND,
		OR,
		XOR,
		AND,
		EQUALITY,
		RELATIONAL,
		SHIFT,
		ADDITIVE,
		MULTIPLICATIVE,
		UNARY,
		MAYBE_NULLABLE_TYPE,
		MAYBE_NULLABLE_WITH_VARIABLE
	}

	private static final TokenSet CONDITIONAL_OR_OPS = TokenSet.create(OROR);
	private static final TokenSet CONDITIONAL_AND_OPS = TokenSet.create(ANDAND);
	private static final TokenSet OR_OPS = TokenSet.create(OR);
	private static final TokenSet XOR_OPS = TokenSet.create(XOR);
	private static final TokenSet AND_OPS = TokenSet.create(AND);
	private static final TokenSet EQUALITY_OPS = TokenSet.create(EQEQ, NTEQ);
	private static final TokenSet RELATIONAL_OPS = TokenSet.create(LT, GT, LTEQ, GTEQ);
	private static final TokenSet SHIFT_OPS = TokenSet.create(LTLT, GTGT);
	private static final TokenSet ADDITIVE_OPS = TokenSet.create(PLUS, MINUS);
	private static final TokenSet MULTIPLICATIVE_OPS = TokenSet.create(MUL, DIV, PERC);
	private static final TokenSet POSTFIX_OPS = TokenSet.create(PLUSPLUS, MINUSMINUS);
	private static final TokenSet PREF_ARITHMETIC_OPS = TokenSet.orSet(POSTFIX_OPS, TokenSet.create(PLUS, MINUS, MUL, AND));
	private static final TokenSet PREFIX_OPS = TokenSet.orSet(PREF_ARITHMETIC_OPS, TokenSet.create(TILDE, EXCL));
	private static final TokenSet ID_OR_SUPER = TokenSet.create(CSharpTokens.IDENTIFIER, BASE_KEYWORD);
	private static final TokenSet THIS_OR_BASE = TokenSet.create(THIS_KEYWORD, BASE_KEYWORD);

	@Nullable
	public static PsiBuilder.Marker parseVariableInitializer(@Nonnull CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		IElementType tokenType = builder.getTokenType();

		if(tokenType == LBRACE)
		{
			return parseArrayInitializer(builder, IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION, set, flags);
		}
		else
		{
			return parse(builder, set);
		}
	}

	@Nullable
	public static PsiBuilder.Marker parse(final CSharpBuilderWrapper builder, ModifierSet set)
	{
		return parse(builder, set, 0);
	}

	@Nullable
	public static PsiBuilder.Marker parse(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		return parseAssignment(builder, set, flags);
	}

	@Nullable
	private static PsiBuilder.Marker parseAssignment(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		final PsiBuilder.Marker left = parseConditional(builder, set, flags);
		if(left == null)
		{
			return null;
		}

		final IElementType tokenType = builder.getTokenTypeGGLL();

		if(ASSIGNMENT_OPERATORS.contains(tokenType) && tokenType != null)
		{
			final PsiBuilder.Marker assignment = left.precede();

			doneOneElementGGLL(builder, tokenType, OPERATOR_REFERENCE, null);

			final PsiBuilder.Marker right = parse(builder, set);
			if(right == null)
			{
				builder.error("Expression expected");
			}

			assignment.done(ASSIGNMENT_EXPRESSION);
			return assignment;
		}

		return left;
	}

	@Nullable
	private static PsiBuilder.Marker parseConditional(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		final PsiBuilder.Marker condition = parseExpression(builder, ExprType.CONDITIONAL_OR, set, flags);
		if(condition == null)
		{
			return null;
		}

		if(builder.getTokenType() == QUESTQUEST)
		{
			final PsiBuilder.Marker nullCoalescing = condition.precede();
			builder.advanceLexer();

			final PsiBuilder.Marker ifNullPart = parse(builder, set);
			if(ifNullPart == null)
			{
				builder.error("Expression expected");
			}
			nullCoalescing.done(NULL_COALESCING_EXPRESSION);
			return nullCoalescing;
		}
		else if(builder.getTokenType() == QUEST)
		{
			final PsiBuilder.Marker ternary = condition.precede();
			builder.advanceLexer();

			final PsiBuilder.Marker truePart = parse(builder, set);
			if(truePart == null)
			{
				builder.error("Expression expected");
				ternary.done(CONDITIONAL_EXPRESSION);
				return ternary;
			}

			if(builder.getTokenType() != COLON)
			{
				builder.error("Expected colon");
				ternary.done(CONDITIONAL_EXPRESSION);
				return ternary;
			}
			builder.advanceLexer();

			final PsiBuilder.Marker falsePart = parseConditional(builder, set, flags);
			if(falsePart == null)
			{
				builder.error("Expression expected");
				ternary.done(CONDITIONAL_EXPRESSION);
				return ternary;
			}

			ternary.done(CONDITIONAL_EXPRESSION);
			return ternary;
		}
		{
			return condition;
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseExpression(final CSharpBuilderWrapper builder, final ExprType type, ModifierSet set, int flags)
	{
		switch(type)
		{
			case CONDITIONAL_OR:
				return parseBinary(builder, ExprType.CONDITIONAL_AND, CONDITIONAL_OR_OPS, set, flags);

			case CONDITIONAL_AND:
				return parseBinary(builder, ExprType.OR, CONDITIONAL_AND_OPS, set, flags);

			case OR:
				return parseBinary(builder, ExprType.XOR, OR_OPS, set, flags);

			case XOR:
				return parseBinary(builder, ExprType.AND, XOR_OPS, set, flags);

			case AND:
				return parseBinary(builder, ExprType.EQUALITY, AND_OPS, set, flags);

			case EQUALITY:
				return parseBinary(builder, ExprType.RELATIONAL, EQUALITY_OPS, set, flags);

			case RELATIONAL:
				return parseRelational(builder, set, flags);

			case SHIFT:
				return parseBinary(builder, ExprType.ADDITIVE, SHIFT_OPS, set, flags);

			case ADDITIVE:
				return parseBinary(builder, ExprType.MULTIPLICATIVE, ADDITIVE_OPS, set, flags);

			case MULTIPLICATIVE:
				return parseBinary(builder, ExprType.UNARY, MULTIPLICATIVE_OPS, set, flags);

			case UNARY:
				return parseUnary(builder, set, flags);

			case MAYBE_NULLABLE_TYPE:
				return parseMaybeNullableType(builder, set, flags);
			case MAYBE_NULLABLE_WITH_VARIABLE:
				PsiBuilder.Marker nullableType = parseMaybeNullableType(builder, set, flags);
				if(nullableType == null)
				{
					return null;
				}

				if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
				{
					PsiBuilder.Marker varIdentifierMarker = builder.mark();
					doneIdentifier(builder, flags);
					varIdentifierMarker.done(IS_VARIABLE);
					return nullableType;
				}
				else
				{
					return nullableType;
				}
			default:
				assert false : "Unexpected type: " + type;
				return null;
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseMaybeNullableType(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		TypeInfo typeInfo = parseType(builder, NONE);
		if(typeInfo == null)
		{
			return null;
		}

		// if we have nullable type - need find colon, or return original marker
		if(typeInfo.isNullable)
		{
			if(builder.getTokenType() == QUEST)
			{
				return typeInfo.marker;
			}
			// o is int? "true" : "false"
			PsiBuilder.Marker marker = parseConditional(builder, set, flags);
			if(marker != null)
			{
				IElementType tokenType = builder.getTokenType();
				marker.rollbackTo();

				if(tokenType == COLON)
				{
					typeInfo.marker.rollbackTo();

					TypeInfo anotherTypeInfo = parseType(builder, WITHOUT_NULLABLE);
					assert anotherTypeInfo != null;
					return anotherTypeInfo.marker;
				}
			}
			return typeInfo.marker;
		}
		else
		{
			return typeInfo.marker;
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseUnary(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		final IElementType tokenType = builder.getTokenType();

		if(PREFIX_OPS.contains(tokenType))
		{
			final PsiBuilder.Marker unary = builder.mark();

			doneOneElementGGLL(builder, tokenType, OPERATOR_REFERENCE, null);

			final PsiBuilder.Marker operand = parseUnary(builder, set, flags);
			if(operand == null)
			{
				builder.error("Expression expected");
			}

			unary.done(PREFIX_EXPRESSION);
			return unary;
		}
		else if(tokenType == LPAR)
		{
			final PsiBuilder.Marker typeCast = builder.mark();
			builder.advanceLexer();

			TypeInfo typeInfo = parseType(builder, LT_GT_HARD_REQUIRE);
			if(typeInfo == null || !expect(builder, RPAR, null))
			{
				typeCast.rollbackTo();
				return parsePostfix(builder, set, flags);
			}

			if(PREF_ARITHMETIC_OPS.contains(builder.getTokenType()) && typeInfo.nativeElementType == null)
			{
				typeCast.rollbackTo();
				return parsePostfix(builder, set, flags);
			}

			final PsiBuilder.Marker expr = parseUnary(builder, set, flags);
			if(expr == null)
			{
				if(!typeInfo.isParameterized)
				{  // cannot parse correct parenthesized expression after correct parameterized type
					typeCast.rollbackTo();
					return parsePostfix(builder, set, flags);
				}
				else
				{
					builder.error("Expression expected");
				}
			}

			typeCast.done(TYPE_CAST_EXPRESSION);
			return typeCast;
		}
		else
		{
			return parsePostfix(builder, set, flags);
		}
	}

	@Nullable
	private static PsiBuilder.Marker parsePostfix(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker operand = parsePrimary(builder, set, flags);
		if(operand == null)
		{
			return null;
		}

		while(POSTFIX_OPS.contains(builder.getTokenType()))
		{
			final PsiBuilder.Marker postfix = operand.precede();

			doneOneElementGGLL(builder, builder.getTokenType(), OPERATOR_REFERENCE, null);

			postfix.done(POSTFIX_EXPRESSION);
			operand = postfix;
		}

		return operand;
	}

	@Nullable
	private static PsiBuilder.Marker parseBinary(final CSharpBuilderWrapper builder, final ExprType type, final TokenSet ops, ModifierSet set, int flags)
	{
		PsiBuilder.Marker result = parseExpression(builder, type, set, flags);
		if(result == null)
		{
			return null;
		}
		IElementType tokenType = builder.getTokenTypeGGLL();
		IElementType currentExprTokenType = tokenType;
		while(true)
		{
			if(tokenType == null || !ops.contains(tokenType))
			{
				break;
			}

			doneOneElementGGLL(builder, tokenType, OPERATOR_REFERENCE, null);

			final PsiBuilder.Marker right = parseExpression(builder, type, set, flags);

			tokenType = builder.getTokenType();
			if(tokenType != null && ops.contains(tokenType) || tokenType == null || !ops.contains(tokenType) || tokenType != currentExprTokenType || right == null)
			{
				// save
				result = result.precede();
				if(right == null)
				{
					builder.error("Expression expected");
				}
				result.done(BINARY_EXPRESSION);
				if(right == null)
				{
					break;
				}
				currentExprTokenType = tokenType;
			}
		}

		return result;
	}

	@Nullable
	private static PsiBuilder.Marker parseRelational(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker left = parseExpression(builder, ExprType.SHIFT, set, flags);
		if(left == null)
		{
			return null;
		}

		IElementType tokenType;
		while((tokenType = builder.getTokenTypeGGLL()) != null)
		{
			final IElementType toCreate;
			final ExprType toParse;
			boolean operatorReference = false;
			if(RELATIONAL_OPS.contains(tokenType))
			{
				toCreate = BINARY_EXPRESSION;
				toParse = ExprType.SHIFT;
				operatorReference = true;
			}
			else if(tokenType == IS_KEYWORD)
			{
				toCreate = IS_EXPRESSION;
				toParse = ExprType.MAYBE_NULLABLE_WITH_VARIABLE;
			}
			else if(tokenType == AS_KEYWORD)
			{
				toCreate = AS_EXPRESSION;
				toParse = ExprType.MAYBE_NULLABLE_TYPE;
			}
			else
			{
				break;
			}

			final PsiBuilder.Marker expression = left.precede();
			if(operatorReference)
			{
				doneOneElementGGLL(builder, builder.getTokenTypeGGLL(), OPERATOR_REFERENCE, null);
			}
			else
			{
				builder.advanceLexerGGLL();
			}

			final PsiBuilder.Marker right = parseExpression(builder, toParse, set, flags);
			if(right == null)
			{
				builder.error(toParse == ExprType.MAYBE_NULLABLE_TYPE || toParse == ExprType.MAYBE_NULLABLE_WITH_VARIABLE ? "Type expected" : "Expression expected");
				expression.done(toCreate);
				return expression;
			}

			expression.done(toCreate);
			left = expression;
		}

		return left;
	}


	@Nullable
	private static PsiBuilder.Marker parsePrimary(final CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker startMarker = builder.mark();

		PsiBuilder.Marker expr = parsePrimaryExpressionStart(builder, set, flags);
		if(expr == null)
		{
			startMarker.drop();
			return null;
		}

		while(true)
		{
			final IElementType tokenType = builder.getTokenType();
			if(tokenType == DOT || tokenType == NULLABE_CALL)
			{
				final PsiBuilder.Marker dotPos = builder.mark();
				builder.advanceLexer();

				IElementType dotTokenType = builder.getTokenType();

				if(dotTokenType == NEW_KEYWORD)
				{
					dotPos.drop();
					expr = parseNewExpression(builder, expr, set, flags);
				}
				else if(dotTokenType == STACKALLOC_KEYWORD)
				{
					dotPos.drop();
					expr = parseStackAllocExpression(builder, expr, set);
				}
				else if(dotTokenType == BASE_KEYWORD)
				{
					dotPos.drop();
					final PsiBuilder.Marker refExpr = expr.precede();
					builder.advanceLexer();
					refExpr.done(referenceExpression(flags));
					expr = refExpr;
				}
				else
				{
					dotPos.drop();
					final PsiBuilder.Marker refExpr = expr.precede();

					if(!expect(builder, ID_OR_SUPER, "Expected identifier"))
					{
						refExpr.done(referenceExpression(flags));
						startMarker.drop();
						return refExpr;
					}

					parseReferenceTypeArgumentList(builder, NONE);
					refExpr.done(referenceExpression(flags));
					expr = refExpr;
				}
			}
			else if(tokenType == ARROW)
			{
				builder.advanceLexer();

				final PsiBuilder.Marker refExpr = expr.precede();

				if(!expect(builder, CSharpTokens.IDENTIFIER, "Expected identifier"))
				{
					refExpr.done(referenceExpression(flags));
					startMarker.drop();
					return refExpr;
				}

				refExpr.done(referenceExpression(flags));
				expr = refExpr;
			}
			else if(tokenType == COLONCOLON)
			{
				builder.advanceLexer();

				final PsiBuilder.Marker refExpr = expr.precede();

				if(!expect(builder, CSharpTokens.IDENTIFIER, "Expected identifier"))
				{
					refExpr.done(referenceExpression(flags));
					startMarker.drop();
					return refExpr;
				}

				parseReferenceTypeArgumentList(builder, NONE);
				refExpr.done(referenceExpression(flags));
				expr = refExpr;
			}
			else if(tokenType == LPAR)
			{
				IElementType expType = exprType(expr);
				if(expType != referenceExpression(flags) && expType != INDEX_ACCESS_EXPRESSION && expType != PARENTHESES_EXPRESSION && expType != METHOD_CALL_EXPRESSION)
				{
					startMarker.drop();
					return expr;
				}

				final PsiBuilder.Marker callExpr = expr.precede();
				parseArgumentList(builder, false, set, flags);
				callExpr.done(METHOD_CALL_EXPRESSION);
				expr = callExpr;
			}
			else if(tokenType == QUEST && builder.lookAhead(1) == LBRACKET || tokenType == LBRACKET)
			{
				final PsiBuilder.Marker arrayAccess = expr.precede();
				if(tokenType == QUEST)
				{
					builder.advanceLexer();
				}
				PsiBuilder.Marker argumentListMarker = builder.mark();

				builder.advanceLexer();

				parseArguments(builder, RBRACKET, false, set, flags);

				if(builder.getTokenType() != RBRACKET)
				{
					builder.error("']' expected");
					argumentListMarker.done(CALL_ARGUMENT_LIST);
					arrayAccess.done(INDEX_ACCESS_EXPRESSION);
					startMarker.drop();
					return arrayAccess;
				}
				builder.advanceLexer();
				argumentListMarker.done(CALL_ARGUMENT_LIST);

				arrayAccess.done(INDEX_ACCESS_EXPRESSION);
				expr = arrayAccess;
			}
			else
			{
				startMarker.drop();
				return expr;
			}
		}
	}

	public static void parseArgumentList(CSharpBuilderWrapper builder, boolean fieldSet, ModifierSet set, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(builder.getTokenType() != LPAR)
		{
			mark.done(CALL_ARGUMENT_LIST);
			return;
		}

		builder.advanceLexer();

		if(builder.getTokenType() == RPAR)
		{
			builder.advanceLexer();
			mark.done(CALL_ARGUMENT_LIST);
			return;
		}

		parseArguments(builder, RPAR, fieldSet, set, flags);

		expect(builder, RPAR, "')' expected");
		mark.done(CALL_ARGUMENT_LIST);
	}

	private static void parseArguments(CSharpBuilderWrapper builder, IElementType stopElement, boolean fieldSet, ModifierSet set, int flags)
	{
		TokenSet stoppers = TokenSet.create(stopElement, CSharpTokens.RBRACE, CSharpTokens.SEMICOLON);

		boolean commaEntered = false;
		while(!builder.eof())
		{
			if(stoppers.contains(builder.getTokenType()))
			{
				if(commaEntered)
				{
					PsiBuilder.Marker mark = builder.mark();
					emptyElement(builder, ERROR_EXPRESSION);
					// call(test,)
					builder.error("Expression expected");
					mark.done(CALL_ARGUMENT);
				}
				break;
			}
			commaEntered = false;
			if(builder.getTokenType() == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == COLON)
			{
				PsiBuilder.Marker marker = builder.mark();
				doneOneElement(builder, CSharpTokens.IDENTIFIER, referenceExpression(flags), null);
				builder.advanceLexer(); // eq
				PsiBuilder.Marker expressionParser = parse(builder, set);
				if(expressionParser == null)
				{
					builder.error("Expression expected");
				}
				marker.done(NAMED_CALL_ARGUMENT);
			}
			else if(fieldSet && builder.getTokenType() == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == EQ)
			{
				PsiBuilder.Marker marker = builder.mark();
				doneOneElement(builder, CSharpTokens.IDENTIFIER, referenceExpression(flags), null);
				builder.advanceLexer(); // eq
				PsiBuilder.Marker expressionParser = parse(builder, set);
				if(expressionParser == null)
				{
					builder.error("Expression expected");
				}
				marker.done(NAMED_FIELD_OR_PROPERTY_SET);
			}
			else
			{
				PsiBuilder.Marker argumentMarker = builder.mark();
				PsiBuilder.Marker marker = parse(builder, set);
				if(marker == null)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					builder.error("Expression expected");
					errorMarker.done(ERROR_EXPRESSION);
				}
				argumentMarker.done(CALL_ARGUMENT);
			}

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
				commaEntered = true;
			}
			else if(!stoppers.contains(builder.getTokenType()))
			{
				builder.error("',' expected");
			}
		}
	}

	@Nullable
	private static PsiBuilder.Marker parsePrimaryExpressionStart(final CSharpBuilderWrapper builder, ModifierSet modifierSet, int flags)
	{
		CSharpLanguageVersion version = builder.getVersion();
		builder.enableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);

		boolean linqState = false, asyncState = false, awaitState = false, nameofState = false;
		if(version.isAtLeast(CSharpLanguageVersion._3_0))
		{
			linqState = builder.enableSoftKeyword(CSharpSoftTokens.FROM_KEYWORD);
		}
		if(version.isAtLeast(CSharpLanguageVersion._4_0))
		{
			asyncState = builder.enableSoftKeyword(CSharpSoftTokens.ASYNC_KEYWORD);
		}
		if(modifierSet.contains(CSharpSoftTokens.ASYNC_KEYWORD))
		{
			awaitState = builder.enableSoftKeyword(CSharpSoftTokens.AWAIT_KEYWORD);
		}
		if(version.isAtLeast(CSharpLanguageVersion._6_0))
		{
			nameofState = builder.enableSoftKeyword(CSharpSoftTokens.NAMEOF_KEYWORD);
		}
		IElementType tokenType = builder.getTokenType();
		if(linqState)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.FROM_KEYWORD);
		}
		if(asyncState)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.ASYNC_KEYWORD);
		}
		if(awaitState)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.AWAIT_KEYWORD);
		}
		if(nameofState)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.NAMEOF_KEYWORD);
		}

		builder.disableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);

		if(tokenType == LBRACE && modifierSet.isAllowShortObjectInitializer())
		{
			PsiBuilder.Marker mark = builder.mark();
			parseNamedFieldOrPropertySetBlock(builder, modifierSet, flags);
			mark.done(SHORT_OBJECT_INITIALIZER_EXPRESSION);
			return mark;
		}

		// if not coloncolon drop
		if(tokenType == CSharpSoftTokens.GLOBAL_KEYWORD && builder.lookAhead(1) != CSharpTokens.COLONCOLON)
		{
			builder.remapBackIfSoft();
			tokenType = builder.getTokenType();
		}

		if(tokenType == CSharpSoftTokens.ASYNC_KEYWORD)
		{
			PsiBuilder.Marker tempMarker = builder.mark();
			builder.advanceLexer();
			boolean isLambdaNext = builder.getTokenType() == CSharpTokens.DELEGATE_KEYWORD || parseLambdaExpression(builder, null, modifierSet.add(CSharpSoftTokens.ASYNC_KEYWORD)) != null;
			tempMarker.rollbackTo();
			if(!isLambdaNext)
			{
				builder.remapBackIfSoft();
				tokenType = builder.getTokenType();
			}
		}

		if(LITERALS.contains(tokenType))
		{
			final PsiBuilder.Marker literal = builder.mark();
			builder.advanceLexer();
			literal.done(CONSTANT_EXPRESSION);
			return literal;
		}

		if(tokenType == NEW_KEYWORD)
		{
			return parseNewExpression(builder, null, modifierSet, flags);
		}

		if(tokenType == STACKALLOC_KEYWORD)
		{
			return parseStackAllocExpression(builder, null, modifierSet);
		}

		if(tokenType == TYPEOF_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, ALLOW_EMPTY_TYPE_ARGUMENTS, null, TYPE_OF_EXPRESSION);
		}

		if(tokenType == NAMEOF_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, NAMEOF_EXPRESSION, modifierSet);
		}

		if(tokenType == DEFAULT_KEYWORD)
		{
			return parseDefaultExpression(builder);
		}

		if(tokenType == SIZEOF_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, NONE, null, SIZE_OF_EXPRESSION);
		}

		if(tokenType == CHECKED_KEYWORD || tokenType == UNCHECKED_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, CHECKED_EXPRESSION, modifierSet);
		}

		if(tokenType == __MAKEREF_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, __MAKEREF_EXPRESSION, modifierSet);
		}

		if(tokenType == __ARGLIST_KEYWORD)
		{
			return parseArglistExpression(builder, modifierSet, flags);
		}

		if(tokenType == __REFTYPE_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, __REFTYPE_EXPRESSION, modifierSet);
		}

		if(tokenType == __REFVALUE_KEYWORD)
		{
			return parseRefValueExpression(builder, modifierSet);
		}

		if(tokenType == DELEGATE_KEYWORD)
		{
			return parseDelegateExpression(builder, modifierSet.remove(CSharpSoftTokens.ASYNC_KEYWORD));
		}

		if(tokenType == REF_KEYWORD || tokenType == OUT_KEYWORD)
		{
			return parseOutRefWrapExpression(builder, modifierSet);
		}

		if(tokenType == AWAIT_KEYWORD)
		{
			return parseAwaitExpression(builder, modifierSet);
		}

		if(tokenType == ASYNC_KEYWORD)
		{
			if(builder.lookAhead(1) == CSharpTokens.DELEGATE_KEYWORD)
			{
				return parseDelegateExpression(builder, modifierSet.add(CSharpSoftTokens.ASYNC_KEYWORD));
			}
			return parseLambdaExpression(builder, null, modifierSet.add(CSharpSoftTokens.ASYNC_KEYWORD));
		}

		if(tokenType == LPAR)
		{
			final PsiBuilder.Marker lambda = parseLambdaAfterParenth(builder, null, modifierSet.remove(CSharpSoftTokens.ASYNC_KEYWORD));
			if(lambda != null)
			{
				return lambda;
			}

			PsiBuilder.Marker tuple = parseTupleExpressionAfterLPar(builder, modifierSet);
			if(tuple != null)
			{
				return tuple;
			}

			final PsiBuilder.Marker parenth = builder.mark();
			builder.advanceLexer();

			final PsiBuilder.Marker inner = parse(builder, modifierSet);
			if(inner == null)
			{
				builder.error("Expression expected");
			}

			if(!expect(builder, RPAR, null))
			{
				if(inner != null)
				{
					builder.error("')' expected");
				}
			}

			parenth.done(PARENTHESES_EXPRESSION);
			return parenth;
		}

		if(tokenType == FROM_KEYWORD)
		{
			PsiBuilder.Marker marker = LinqParsing.parseLinqExpression(builder, modifierSet);
			if(marker == null)
			{
				assert builder.getTokenType() == FROM_KEYWORD : builder.getTokenText() + ":" + builder.getTokenType();
				builder.remapBackIfSoft();
				tokenType = builder.getTokenType();
			}
			else
			{
				return marker;
			}
		}

		if(tokenType == GLOBAL_KEYWORD)
		{
			PsiBuilder.Marker refExpr = builder.mark();
			builder.advanceLexer();
			refExpr.done(referenceExpression(flags));
			return refExpr;
		}

		if(tokenType == CSharpTokens.IDENTIFIER)
		{
			if(builder.lookAhead(1) == DARROW)
			{
				return parseLambdaExpression(builder, null, modifierSet.remove(CSharpSoftTokens.ASYNC_KEYWORD));
			}

			PsiBuilder.Marker refExpr = builder.mark();

			builder.advanceLexer();
			parseReferenceTypeArgumentList(builder, NONE);
			refExpr.done(referenceExpression(flags));
			return refExpr;
		}

		if(NATIVE_TYPES.contains(tokenType))
		{
			if(tokenType == CSharpTokens.VOID_KEYWORD)
			{
				return null;
			}

			PsiBuilder.Marker refExpr = builder.mark();

			builder.advanceLexer();
			refExpr.done(referenceExpression(flags));
			return refExpr;
		}

		if(THIS_OR_BASE.contains(tokenType))
		{
			PsiBuilder.Marker expr = builder.mark();
			builder.advanceLexer();
			expr.done(referenceExpression(flags));
			return expr;
		}

		return null;
	}

	@Nonnull
	private static PsiBuilder.Marker parseDefaultExpression(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker marker = builder.mark();
		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			if(expect(builder, LPAR, "'(' expected"))
			{
				if(parseType(builder, NONE, TokenSet.EMPTY) == null)
				{
					builder.error("Type expected");
				}
				expect(builder, RPAR, "')' expected");
			}
		}

		marker.done(DEFAULT_EXPRESSION);
		return marker;
	}

	private static PsiBuilder.Marker parseTupleExpressionAfterLPar(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker marker = builder.mark();
		builder.advanceLexer();

		// expression like '(name: test)'
		if(builder.getTokenType() == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == CSharpTokens.COLON)
		{
			marker.rollbackTo();
			return parseTupleExpression(builder, set);
		}
		else
		{
			PsiBuilder.Marker expression = parse(builder, set);
			if(expression == null || builder.getTokenType() != CSharpTokens.COMMA)
			{
				marker.rollbackTo();
				return null;
			}
			marker.rollbackTo();
			return parseTupleExpression(builder, set);
		}
	}

	private static PsiBuilder.Marker parseTupleExpression(CSharpBuilderWrapper builder, ModifierSet set)
	{
		if(builder.getTokenType() == LPAR)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();

			while(true)
			{
				parseTupleElement(builder, set);
				if(builder.getTokenType() != COMMA)
				{
					break;
				}
				else
				{
					builder.advanceLexer();
				}
			}

			expect(builder, RPAR, "')' expected");
			mark.done(TUPLE_EXPRESSION);
			return mark;
		}
		return null;
	}

	private static boolean parseTupleElement(CSharpBuilderWrapper builder, ModifierSet set)
	{
		if(builder.getTokenType() == CSharpTokens.COMMA || builder.getTokenType() == CSharpTokens.RPAR)
		{
			return false;
		}

		boolean valid = true;
		PsiBuilder.Marker mark = builder.mark();
		if(builder.getTokenType() == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == CSharpTokens.COLON)
		{
			doneIdentifier(builder, NONE);
			builder.advanceLexer(); // colon
			if(parse(builder, set) == null)
			{
				builder.error("Expression expected");
				valid = false;
			}
		}
		else
		{
			if(parse(builder, set) == null)
			{
				builder.error("Expression expected");
				valid = false;
			}
		}
		mark.done(CSharpElements.TUPLE_ELEMENT);
		return valid;
	}

	private static PsiBuilder.Marker parseArglistExpression(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			parseArgumentList(builder, false, set, flags);
		}

		mark.done(__ARGLIST_EXPRESSION);
		return mark;
	}

	private static PsiBuilder.Marker parseRefValueExpression(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}

			expect(builder, COMMA, "',' expected");

			if(parseType(builder) == null)
			{
				builder.error("Type expected");
			}
			expect(builder, RPAR, "')' expected");
		}

		mark.done(__REFVALUE_EXPRESSION);
		return mark;
	}

	private static PsiBuilder.Marker parseOutRefWrapExpression(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		PsiBuilder.Marker possibleVarMark = builder.mark();

		TypeInfo type = parseType(builder, VAR_SUPPORT);
		if(type != null && !type.isArrayError && builder.getTokenType() == CSharpTokens.IDENTIFIER)
		{
			doneIdentifier(builder, NONE);

			possibleVarMark.done(OUT_REF_VARIABLE);

			mark.done(OUT_REF_VARIABLE_EXPRESSION);
		}
		else
		{
			possibleVarMark.rollbackTo();

			if(parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}

			mark.done(OUT_REF_WRAP_EXPRESSION);
		}
		return mark;
	}

	private static PsiBuilder.Marker parseAwaitExpression(@Nonnull CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker marker = builder.mark();
		builder.advanceLexer();

		if(parse(builder, set) == null)
		{
			builder.error("Expression expected");
		}

		marker.done(AWAIT_EXPRESSION);
		return marker;
	}

	private static PsiBuilder.Marker parseDelegateExpression(@Nonnull CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker marker = builder.mark();

		if(builder.getTokenType() == CSharpSoftTokens.ASYNC_KEYWORD)
		{
			builder.advanceLexer();
		}
		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			MethodParsing.parseParameterList(builder, NONE, RPAR, set);
		}

		if(builder.getTokenType() == LBRACE)
		{
			StatementParsing.parse(builder, set);
		}
		else
		{
			builder.error("'{' expected");
		}

		marker.done(DELEGATE_EXPRESSION);
		return marker;
	}

	@Nullable
	private static PsiBuilder.Marker parseLambdaAfterParenth(final CSharpBuilderWrapper builder, @Nullable final PsiBuilder.Marker typeList, ModifierSet set)
	{
		final boolean isLambda;

		final IElementType nextToken1 = builder.lookAhead(1);
		final IElementType nextToken2 = builder.lookAhead(2);
		if(nextToken1 == RPAR && nextToken2 == DARROW)
		{
			isLambda = true;
		}
		else
		{
			if(nextToken2 == COMMA || nextToken2 == RPAR && builder.lookAhead(3) == DARROW)
			{
				isLambda = true;
			}
			else if(nextToken2 == DARROW)
			{
				isLambda = false;
			}
			else
			{
				boolean arrow = false;

				final PsiBuilder.Marker marker = builder.mark();
				while(!builder.eof())
				{
					builder.advanceLexer();
					final IElementType tokenType = builder.getTokenType();
					if(tokenType == DARROW)
					{
						arrow = true;
						break;
					}
					if(tokenType == RPAR)
					{
						arrow = builder.lookAhead(1) == DARROW;
						break;
					}
					else if(tokenType == LPAR || tokenType == SEMICOLON || tokenType == LBRACE || tokenType == RBRACE)
					{
						break;
					}
				}
				marker.rollbackTo();

				isLambda = arrow;
			}
		}

		return isLambda ? parseLambdaExpression(builder, typeList, set) : null;
	}

	@Nullable
	private static PsiBuilder.Marker parseLambdaExpression(final CSharpBuilderWrapper builder, @Nullable final PsiBuilder.Marker typeList, ModifierSet set)
	{
		PsiBuilder.Marker start = typeList != null ? typeList.precede() : builder.mark();

		if(builder.getTokenType() == ASYNC_KEYWORD)
		{
			builder.advanceLexer();
		}

		parseLambdaParameterList(builder);

		if(!expect(builder, DARROW, null))
		{
			start.rollbackTo();
			return null;
		}

		final PsiBuilder.Marker body;
		if(builder.getTokenType() == LBRACE)
		{
			body = StatementParsing.parse(builder, set);
		}
		else
		{
			body = parse(builder, set);
		}

		if(body == null)
		{
			builder.error("'}' expected");
		}

		start.done(LAMBDA_EXPRESSION);
		return start;
	}

	private static void parseLambdaParameterList(final CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		boolean lpar = expect(builder, LPAR, null);

		if(!lpar)
		{
			parseLambdaParameter(builder, true);
		}
		else
		{
			if(builder.getTokenType() != CSharpTokens.RPAR)
			{
				while(!builder.eof())
				{
					parseLambdaParameter(builder, false);

					if(builder.getTokenType() == COMMA)
					{
						builder.advanceLexer();
					}
					else if(builder.getTokenType() == CSharpTokens.RPAR)
					{
						break;
					}
					else
					{
						PsiBuilder.Marker errorMarker = builder.mark();
						builder.advanceLexer();
						errorMarker.error("Expected comma");
					}
				}
			}

			expect(builder, RPAR, "')' expected");
		}

		mark.done(LAMBDA_PARAMETER_LIST);
	}

	private static void parseLambdaParameter(CSharpBuilderWrapper builder, boolean single)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(single)
		{
			expectOrReportIdentifier(builder, 0);

			mark.done(CSharpElements.LAMBDA_PARAMETER);
		}
		else if(builder.getTokenType() == CSharpTokens.OUT_KEYWORD || builder.getTokenType() == CSharpTokens.REF_KEYWORD)
		{
			PsiBuilder.Marker modifierList = builder.mark();
			builder.advanceLexer();
			modifierList.done(CSharpElements.MODIFIER_LIST);

			if(parseType(builder) != null)
			{
				expectOrReportIdentifier(builder, 0);

				mark.done(LAMBDA_PARAMETER);
			}
			else
			{
				mark.error("Identifier expected");
			}
		}
		else
		{
			boolean wasIdentifier = builder.getTokenType() == CSharpTokens.IDENTIFIER;

			emptyElement(builder, CSharpElements.MODIFIER_LIST);

			TypeInfo typeInfo = parseType(builder);

			if(typeInfo != null)
			{
				if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
				{
					expectOrReportIdentifier(builder, 0);

					mark.done(LAMBDA_PARAMETER);
				}
				else if(wasIdentifier)
				{
					typeInfo.marker.rollbackTo();

					expectOrReportIdentifier(builder, 0);

					mark.done(LAMBDA_PARAMETER);
				}
				else
				{
					mark.drop();

					reportIdentifier(builder, 0);
				}
			}
			else
			{
				mark.error("Identifier expected");
			}
		}
	}

	public static void parseConstructorSuperCall(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker marker = builder.mark();
		if(THIS_OR_BASE.contains(builder.getTokenType()) || builder.getTokenType() == CSharpTokens.IDENTIFIER)
		{
			if(builder.getTokenType() == CSharpTokens.IDENTIFIER)
			{
				builder.error("Expected 'base' or 'this'");
			}

			doneOneElement(builder, builder.getTokenType(), referenceExpression(flags), null);

			if(builder.getTokenType() == LPAR)
			{
				parseArgumentList(builder, false, set, flags);
			}
			else
			{
				builder.error("'(' expected");
			}

			marker.done(CONSTRUCTOR_SUPER_CALL_EXPRESSION);
		}
		else
		{
			builder.error("Expected 'base' or 'this'");
			marker.drop();
		}
	}

	private static enum AfterNewParsingTarget
	{
		NONE,
		ANONYM_PROPERTY_SET_LIST,
		PROPERTY_SET_LIST,
		ARRAY_INITIALZER,
		DICTIONARY_INITIALZER
	}

	private static PsiBuilder.Marker parseNewExpression(CSharpBuilderWrapper builder, PsiBuilder.Marker mark, ModifierSet set, int flags)
	{
		PsiBuilder.Marker newExpr = (mark != null ? mark.precede() : builder.mark());

		builder.advanceLexer();

		TypeInfo typeInfo = parseType(builder, BRACKET_RETURN_BEFORE);

		boolean forceArray = false;

		while(parseArrayLength(builder, set))
		{
			forceArray = true;

			// we can eat only one [] if not type
			if(typeInfo == null)
			{
				break;
			}
		}

		boolean argumentsPassed = false;
		if(builder.getTokenType() == LPAR)
		{
			parseArgumentList(builder, false, set, flags);
			argumentsPassed = true;
		}

		AfterNewParsingTarget target = getTarget(builder, forceArray, typeInfo);
		switch(target)
		{
			case NONE:
				if(!argumentsPassed && !forceArray)
				{
					builder.error("'(' expected");
				}
				break;
			case PROPERTY_SET_LIST:
				parseNamedFieldOrPropertySetBlock(builder, set, flags);
				break;
			case ANONYM_PROPERTY_SET_LIST:
				parseAnonymFieldOrPropertySetBlock(builder, set, flags);
				break;
			case DICTIONARY_INITIALZER:
				parseDictionaryInitializerList(builder, set);
				break;
			case ARRAY_INITIALZER:
				parseArrayInitializer(builder, ARRAY_INITIALIZER, set, flags);
				break;
		}

		newExpr.done(NEW_EXPRESSION);
		return newExpr;
	}

	private static boolean parseArrayLength(@Nonnull CSharpBuilderWrapper builder, ModifierSet set)
	{
		if(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker arrayMarker = builder.mark();
			builder.advanceLexer();

			while(true)
			{
				parse(builder, set);
				if(builder.getTokenType() != COMMA)
				{
					break;
				}
				else
				{
					builder.advanceLexer();
				}
			}

			expect(builder, RBRACKET, "']' expected");
			arrayMarker.done(NEW_ARRAY_LENGTH);
			return true;
		}
		return false;
	}

	private static PsiBuilder.Marker parseStackAllocExpression(CSharpBuilderWrapper builder, PsiBuilder.Marker mark, ModifierSet set)
	{
		PsiBuilder.Marker newExpr = (mark != null ? mark.precede() : builder.mark());

		builder.advanceLexer();

		TypeInfo typeMarker = parseType(builder, BRACKET_RETURN_BEFORE);
		if(typeMarker == null)
		{
			builder.error("Expected type");
		}

		while(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker arrayMarker = builder.mark();
			builder.advanceLexer();

			while(true)
			{
				parse(builder, set);
				if(builder.getTokenType() != COMMA)
				{
					break;
				}
				else
				{
					builder.advanceLexer();
				}
			}

			expect(builder, RBRACKET, "']' expected");
			arrayMarker.done(NEW_ARRAY_LENGTH);
		}

		newExpr.done(STACKALLOC_EXPRESSION);
		return newExpr;
	}

	@Nonnull
	private static AfterNewParsingTarget getTarget(CSharpBuilderWrapper builderWrapper, boolean forceArray, TypeInfo typeInfo)
	{
		if(typeInfo != null && typeInfo.isArray)
		{
			return AfterNewParsingTarget.ARRAY_INITIALZER;
		}

		if(builderWrapper.getTokenType() != LBRACE)
		{
			return AfterNewParsingTarget.NONE;
		}

		if(forceArray)
		{
			return AfterNewParsingTarget.ARRAY_INITIALZER;
		}

		// force property list, anonym object
		if(typeInfo == null)
		{
			return AfterNewParsingTarget.ANONYM_PROPERTY_SET_LIST;
		}

		if(builderWrapper.lookAhead(1) == LBRACKET)
		{
			return AfterNewParsingTarget.DICTIONARY_INITIALZER;
		}

		if(builderWrapper.lookAhead(1) == CSharpTokens.IDENTIFIER && builderWrapper.lookAhead(2) == EQ)
		{
			return AfterNewParsingTarget.PROPERTY_SET_LIST;
		}
		else
		{
			return AfterNewParsingTarget.ARRAY_INITIALZER;
		}
	}

	private static PsiBuilder.Marker parseArrayInitializer(CSharpBuilderWrapper builderWrapper, IElementType to, ModifierSet set, int flags)
	{
		if(builderWrapper.getTokenType() != LBRACE)
		{
			return null;
		}

		PsiBuilder.Marker marker = builderWrapper.mark();

		builderWrapper.advanceLexer();

		while(!builderWrapper.eof())
		{
			if(builderWrapper.getTokenType() == RBRACE)
			{
				break;
			}

			boolean enteredValue = false;
			if(builderWrapper.getTokenType() == LBRACE)
			{
				parseArrayInitializerCompositeValue(builderWrapper, set, flags);
				enteredValue = true;
			}
			else
			{
				PsiBuilder.Marker temp = builderWrapper.mark();
				if(parse(builderWrapper, set) != null)
				{
					enteredValue = true;
					temp.done(ARRAY_INITIALIZER_SINGLE_VALUE);
				}
				else
				{
					// for example we entered keyword, it cant be parsed as expression
					if(builderWrapper.getTokenType() != COMMA && builderWrapper.getTokenType() != RBRACE)
					{
						// we entered value
						enteredValue = true;
						builderWrapper.advanceLexer();
						temp.error("Expression expected");
					}
					else
					{
						temp.drop();
					}
				}
			}

			if(builderWrapper.getTokenType() == COMMA)
			{
				if(!enteredValue)
				{
					builderWrapper.error("Expression expected");
				}
				builderWrapper.advanceLexer();
			}
			else if(builderWrapper.getTokenType() != RBRACE)
			{
				builderWrapper.error("Comma expected");
			}
		}

		expect(builderWrapper, RBRACE, "'}' expected");
		marker.done(to);
		return marker;
	}

	private static PsiBuilder.Marker parseArrayInitializerCompositeValue(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		if(builder.getTokenType() != LBRACE)
		{
			return null;
		}

		PsiBuilder.Marker headerMarker = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() == RBRACE)
		{
			builder.advanceLexer();
		}
		else
		{
			parseArguments(builder, RBRACE, false, set, flags);

			expect(builder, RBRACE, "'}' expected");
		}

		headerMarker.done(ARRAY_INITIALIZER_COMPOSITE_VALUE);
		return headerMarker;
	}

	private static void parseAnonymFieldOrPropertySetBlock(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(!expect(builder, RBRACE, null))
		{
			while(!builder.eof())
			{
				if(builder.getTokenType() == RBRACE)
				{
					break;
				}

				PsiBuilder.Marker tempMarker = builder.mark();
				PsiBuilder.Marker expressionMarker = parse(builder, set);
				if(expressionMarker != null)
				{
					IElementType elementType = SharedParsingHelpers.exprType(expressionMarker);
					if(elementType == CSharpElements.ASSIGNMENT_EXPRESSION)
					{
						tempMarker.rollbackTo();
						parseNamedFieldOrPropertySet(builder, set, flags);
					}
					else
					{
						if(elementType != referenceExpression(flags))
						{
							tempMarker.error("Can use assign or reference expression");
						}
						else
						{
							tempMarker.done(CSharpElements.ANONYM_FIELD_OR_PROPERTY_SET);
						}
					}
				}
				else
				{
					tempMarker.drop();
				}

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() != RBRACE)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					errorMarker.error("',' expected");
				}
			}
			expect(builder, RBRACE, "'}' expected");
		}

		mark.done(FIELD_OR_PROPERTY_SET_BLOCK);
	}

	private static void parseNamedFieldOrPropertySetBlock(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(!expect(builder, RBRACE, null))
		{
			while(!builder.eof())
			{
				if(builder.getTokenType() == RBRACE)
				{
					break;
				}

				if(parseNamedFieldOrPropertySet(builder, set, flags) == null)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					errorMarker.error("Identifier expected");
				}

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() != RBRACE)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					errorMarker.error("',' expected");
				}
			}
			expect(builder, RBRACE, "'}' expected");
		}

		mark.done(FIELD_OR_PROPERTY_SET_BLOCK);
	}

	private static PsiBuilder.Marker parseNamedFieldOrPropertySet(CSharpBuilderWrapper builder, ModifierSet set, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(doneOneElement(builder, CSharpTokens.IDENTIFIER, referenceExpression(flags), "Identifier expected"))
		{
			if(expect(builder, EQ, "'=' expected"))
			{
				if(parse(builder, set.setAllowShortObjectInitializer()) == null)
				{
					builder.error("Expression expected");
				}
			}
			mark.done(NAMED_FIELD_OR_PROPERTY_SET);
			return mark;
		}
		else
		{
			mark.rollbackTo();
			return null;
		}
	}

	private static void parseDictionaryInitializerList(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(!expect(builder, RBRACE, null))
		{
			while(!builder.eof())
			{
				if(builder.getTokenType() == RBRACE)
				{
					break;
				}

				if(parseDictionaryInitializer(builder, set) == null)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					errorMarker.error("'[' expected");
				}

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() != RBRACE)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					errorMarker.error("',' expected");
				}
			}
			expect(builder, RBRACE, "'}' expected");
		}

		mark.done(DICTIONARY_INITIALIZER_LIST);
	}

	private static PsiBuilder.Marker parseDictionaryInitializer(CSharpBuilderWrapper builder, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker firstArgumentMarker = builder.mark();
			builder.advanceLexer();
			if(parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
			expect(builder, RBRACKET, "']' expected");
			firstArgumentMarker.done(CALL_ARGUMENT);

			if(expect(builder, EQ, "'=' expected"))
			{
				PsiBuilder.Marker valueArgumentMarker = builder.mark();
				if(parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
				valueArgumentMarker.done(CALL_ARGUMENT);
			}

			mark.done(DICTIONARY_INITIALIZER);
			return mark;
		}
		else
		{
			mark.rollbackTo();
			return null;
		}
	}

	private static PsiBuilder.Marker parseExpressionWithTypeInLParRPar(CSharpBuilderWrapper builder, int flags, PsiBuilder.Marker mark, IElementType to)
	{
		PsiBuilder.Marker newMarker = mark == null ? builder.mark() : mark.precede();
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parseType(builder, flags, TokenSet.EMPTY) == null)
			{
				builder.error("Type expected");
			}
			expect(builder, RPAR, "')' expected");
		}
		newMarker.done(to);
		return newMarker;
	}

	private static PsiBuilder.Marker parseExpressionWithExpressionInLParRPar(CSharpBuilderWrapper builder, PsiBuilder.Marker mark, IElementType to, ModifierSet set)
	{
		PsiBuilder.Marker newMarker = mark == null ? builder.mark() : mark.precede();
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parse(builder, set) == null)
			{
				builder.error("Expression expected");
			}
			expect(builder, RPAR, "')' expected");
		}
		newMarker.done(to);
		return newMarker;
	}

	public static class ReferenceInfo
	{
		public boolean isParameterized;
		public PsiBuilder.Marker marker;

		public ReferenceInfo(boolean isParameterized, PsiBuilder.Marker marker)
		{
			this.isParameterized = isParameterized;
			this.marker = marker;
		}
	}

	public static ReferenceInfo parseQualifiedReference(@Nonnull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker prevMarker)
	{
		return parseQualifiedReference(builder, prevMarker, NONE, TokenSet.EMPTY);
	}

	public static ReferenceInfo parseQualifiedReference(@Nonnull CSharpBuilderWrapper builder, @Nullable final PsiBuilder.Marker prevMarker, int flags, @Nonnull TokenSet nameStopperSet)
	{
		if(prevMarker != null)
		{
			builder.advanceLexer(); // skip dot or coloncolon
		}

		PsiBuilder.Marker marker = prevMarker == null ? builder.mark() : prevMarker;

		ReferenceInfo referenceInfo = new ReferenceInfo(false, marker);

		if(prevMarker == null)
		{
			builder.enableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);
			IElementType tokenType = builder.getTokenType();
			builder.disableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);

			if(tokenType == CSharpSoftTokens.GLOBAL_KEYWORD && builder.lookAhead(1) == CSharpTokens.COLONCOLON)
			{
				builder.advanceLexer(); // global

				marker.done(referenceExpression(flags));

				return parseQualifiedReference(builder, marker.precede(), flags, nameStopperSet);
			}
			else
			{
				builder.remapBackIfSoft();
			}
		}

		if(expect(builder, CSharpTokens.IDENTIFIER, "Identifier expected"))
		{
			referenceInfo.isParameterized = parseReferenceTypeArgumentList(builder, flags) != null;

			marker.done(referenceExpression(flags));

			// inside doc - PLUS used for nested type reference
			if(builder.getTokenType() == DOT || BitUtil.isSet(flags, INSIDE_DOC) && builder.getTokenType() == PLUS)
			{
				// if after dot we found stoppers, name expected - but we done
				if(nameStopperSet.contains(builder.lookAhead(1)) || nameStopperSet.contains(builder.lookAhead(2)))
				{
					return referenceInfo;
				}
				referenceInfo = parseQualifiedReference(builder, marker.precede(), flags, nameStopperSet);
			}
		}
		else
		{
			marker.drop();
			return null;
		}

		return referenceInfo;
	}

	@Nullable
	private static PsiBuilder.Marker parseReferenceTypeArgumentList(@Nonnull CSharpBuilderWrapper builder, int flags)
	{
		IElementType startElementType = BitUtil.isSet(flags, INSIDE_DOC) ? LBRACE : LT;

		if(builder.getTokenType() != startElementType)
		{
			return null;
		}

		if(BitUtil.isSet(flags, ALLOW_EMPTY_TYPE_ARGUMENTS))
		{
			if(BitUtil.isSet(flags, STUB_SUPPORT))
			{
				throw new IllegalArgumentException("Empty type arguments is not allowed inside stub tree");
			}
			PsiBuilder.Marker marker = parseReferenceEmptyTypeArgumentListImpl(builder);
			if(marker != null)
			{
				return marker;
			}
		}
		return parseReferenceTypeArgumentListImpl(builder, flags);
	}

	@Nullable
	private static PsiBuilder.Marker parseReferenceEmptyTypeArgumentListImpl(@Nonnull CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(builder.getTokenType() == GT)
		{
			builder.advanceLexer();
		}
		else
		{
			while(!builder.eof())
			{
				if(builder.getTokenType() == GT)
				{
					break;
				}

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else
				{
					mark.rollbackTo();
					return null;
				}
			}

			if(builder.getTokenType() == GT)
			{
				builder.advanceLexer();
			}
			else
			{
				mark.rollbackTo();
				return null;
			}
		}
		mark.done(CSharpElements.EMPTY_TYPE_ARGUMENTS);
		return mark;
	}

	@Nullable
	private static PsiBuilder.Marker parseReferenceTypeArgumentListImpl(@Nonnull CSharpBuilderWrapper builder, int flags)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		IElementType stopElementType = BitUtil.isSet(flags, INSIDE_DOC) ? RBRACE : GT;
		if(builder.getTokenType() == stopElementType)
		{
			builder.error("Expected type");
			builder.advanceLexer();
		}
		else
		{
			while(!builder.eof())
			{
				TypeInfo marker = parseType(builder, flags);
				if(marker == null)
				{
					builder.error("Expected type");
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

			if(builder.getTokenType() == stopElementType)
			{
				builder.advanceLexer();
			}
			else
			{
				mark.rollbackTo();
				return null;
			}
		}
		mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.TYPE_ARGUMENTS : CSharpElements.TYPE_ARGUMENTS);
		return mark;
	}

	@Nonnull
	private static IElementType referenceExpression(int flags)
	{
		return BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.REFERENCE_EXPRESSION : CSharpElements.REFERENCE_EXPRESSION;
	}
}
