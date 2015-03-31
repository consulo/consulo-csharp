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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.decl.MethodParsing;
import org.mustbe.consulo.csharp.lang.parser.stmt.StatementParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.BitUtil;
import lombok.val;

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
		MAYBE_NULLABLE_TYPE
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
	private static final TokenSet ID_OR_SUPER = TokenSet.create(IDENTIFIER, BASE_KEYWORD);
	private static final TokenSet THIS_OR_BASE = TokenSet.create(THIS_KEYWORD, BASE_KEYWORD);

	@Nullable
	public static PsiBuilder.Marker parseVariableInitializer(@NotNull CSharpBuilderWrapper builder)
	{
		IElementType tokenType = builder.getTokenType();

		if(tokenType == LBRACE)
		{
			return parseArrayInitializer(builder, IMPLICIT_ARRAY_INITIALIZATION_EXPRESSION);
		}
		else
		{
			return parse(builder);
		}
	}

	@Nullable
	public static PsiBuilder.Marker parse(final CSharpBuilderWrapper builder)
	{
		return parseAssignment(builder);
	}

	@Nullable
	private static PsiBuilder.Marker parseAssignment(final CSharpBuilderWrapper builder)
	{
		final PsiBuilder.Marker left = parseConditional(builder);
		if(left == null)
		{
			return null;
		}

		final IElementType tokenType = builder.getTokenTypeGGLL();

		if(ASSIGNMENT_OPERATORS.contains(tokenType) && tokenType != null)
		{
			final PsiBuilder.Marker assignment = left.precede();

			doneOneElementGGLL(builder, tokenType, OPERATOR_REFERENCE, null);

			final PsiBuilder.Marker right = parse(builder);
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
	private static PsiBuilder.Marker parseConditional(final CSharpBuilderWrapper builder)
	{
		final PsiBuilder.Marker condition = parseExpression(builder, ExprType.CONDITIONAL_OR);
		if(condition == null)
		{
			return null;
		}

		if(builder.getTokenType() == QUESTQUEST)
		{
			final PsiBuilder.Marker nullCoalescing = condition.precede();
			builder.advanceLexer();

			final PsiBuilder.Marker ifNullPart = parse(builder);
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

			final PsiBuilder.Marker truePart = parse(builder);
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

			final PsiBuilder.Marker falsePart = parseConditional(builder);
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
	private static PsiBuilder.Marker parseExpression(final CSharpBuilderWrapper builder, final ExprType type)
	{
		switch(type)
		{
			case CONDITIONAL_OR:
				return parseBinary(builder, ExprType.CONDITIONAL_AND, CONDITIONAL_OR_OPS);

			case CONDITIONAL_AND:
				return parseBinary(builder, ExprType.OR, CONDITIONAL_AND_OPS);

			case OR:
				return parseBinary(builder, ExprType.XOR, OR_OPS);

			case XOR:
				return parseBinary(builder, ExprType.AND, XOR_OPS);

			case AND:
				return parseBinary(builder, ExprType.EQUALITY, AND_OPS);

			case EQUALITY:
				return parseBinary(builder, ExprType.RELATIONAL, EQUALITY_OPS);

			case RELATIONAL:
				return parseRelational(builder);

			case SHIFT:
				return parseBinary(builder, ExprType.ADDITIVE, SHIFT_OPS);

			case ADDITIVE:
				return parseBinary(builder, ExprType.MULTIPLICATIVE, ADDITIVE_OPS);

			case MULTIPLICATIVE:
				return parseBinary(builder, ExprType.UNARY, MULTIPLICATIVE_OPS);

			case UNARY:
				return parseUnary(builder);

			case MAYBE_NULLABLE_TYPE:
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
					PsiBuilder.Marker marker = parseConditional(builder);
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
			default:
				assert false : "Unexpected type: " + type;
				return null;
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseUnary(final CSharpBuilderWrapper builder)
	{
		final IElementType tokenType = builder.getTokenType();

		if(PREFIX_OPS.contains(tokenType))
		{
			final PsiBuilder.Marker unary = builder.mark();

			doneOneElementGGLL(builder, tokenType, OPERATOR_REFERENCE, null);

			final PsiBuilder.Marker operand = parseUnary(builder);
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

			val typeInfo = parseType(builder, LT_GT_HARD_REQUIRE);
			if(typeInfo == null || !expect(builder, RPAR, null))
			{
				typeCast.rollbackTo();
				return parsePostfix(builder);
			}

			if(PREF_ARITHMETIC_OPS.contains(builder.getTokenType()) && typeInfo.nativeElementType == null)
			{
				typeCast.rollbackTo();
				return parsePostfix(builder);
			}

			final PsiBuilder.Marker expr = parseUnary(builder);
			if(expr == null)
			{
				if(!typeInfo.isParameterized)
				{  // cannot parse correct parenthesized expression after correct parameterized type
					typeCast.rollbackTo();
					return parsePostfix(builder);
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
			return parsePostfix(builder);
		}
	}

	@Nullable
	private static PsiBuilder.Marker parsePostfix(final CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker operand = parsePrimary(builder);
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
	private static PsiBuilder.Marker parseBinary(final CSharpBuilderWrapper builder, final ExprType type, final TokenSet ops)
	{
		PsiBuilder.Marker result = parseExpression(builder, type);
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

			final PsiBuilder.Marker right = parseExpression(builder, type);

			tokenType = builder.getTokenType();
			if(tokenType != null && ops.contains(tokenType) || tokenType == null || !ops.contains(tokenType) || tokenType != currentExprTokenType ||
					right == null)
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
	private static PsiBuilder.Marker parseRelational(final CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker left = parseExpression(builder, ExprType.SHIFT);
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
				toParse = ExprType.MAYBE_NULLABLE_TYPE;
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

			final PsiBuilder.Marker right = parseExpression(builder, toParse);
			if(right == null)
			{
				builder.error(toParse == ExprType.MAYBE_NULLABLE_TYPE ? "Type expected" : "Expression expected");
				expression.done(toCreate);
				return expression;
			}

			expression.done(toCreate);
			left = expression;
		}

		return left;
	}


	@Nullable
	private static PsiBuilder.Marker parsePrimary(final CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker startMarker = builder.mark();

		PsiBuilder.Marker expr = parsePrimaryExpressionStart(builder);
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
					expr = parseNewExpression(builder, expr);
				}
				else if(dotTokenType == STACKALLOC_KEYWORD)
				{
					dotPos.drop();
					expr = parseStackAllocExpression(builder, expr);
				}
				else if(dotTokenType == BASE_KEYWORD)
				{
					dotPos.drop();
					final PsiBuilder.Marker refExpr = expr.precede();
					builder.advanceLexer();
					refExpr.done(REFERENCE_EXPRESSION);
					expr = refExpr;
				}
				else
				{
					dotPos.drop();
					final PsiBuilder.Marker refExpr = expr.precede();

					if(!expect(builder, ID_OR_SUPER, "expected.identifier"))
					{
						refExpr.done(REFERENCE_EXPRESSION);
						startMarker.drop();
						return refExpr;
					}

					parseReferenceTypeArgumentList(builder, NONE);
					refExpr.done(REFERENCE_EXPRESSION);
					expr = refExpr;
				}
			}
			else if(tokenType == ARROW)
			{
				builder.advanceLexer();

				final PsiBuilder.Marker refExpr = expr.precede();

				if(!expect(builder, IDENTIFIER, "expected.identifier"))
				{
					refExpr.done(REFERENCE_EXPRESSION);
					startMarker.drop();
					return refExpr;
				}

				refExpr.done(REFERENCE_EXPRESSION);
				expr = refExpr;
			}
			else if(tokenType == COLONCOLON)
			{
				builder.advanceLexer();

				final PsiBuilder.Marker refExpr = expr.precede();

				if(!expect(builder, IDENTIFIER, "expected.identifier"))
				{
					refExpr.done(REFERENCE_EXPRESSION);
					startMarker.drop();
					return refExpr;
				}

				parseReferenceTypeArgumentList(builder, NONE);
				refExpr.done(REFERENCE_EXPRESSION);
				expr = refExpr;
			}
			else if(tokenType == LPAR)
			{
				if(exprType(expr) != REFERENCE_EXPRESSION && exprType(expr) != ARRAY_ACCESS_EXPRESSION)
				{
					startMarker.drop();
					return expr;
				}

				final PsiBuilder.Marker callExpr = expr.precede();
				parseArgumentList(builder, false);
				callExpr.done(METHOD_CALL_EXPRESSION);
				expr = callExpr;
			}
			else if(tokenType == LBRACKET)
			{
				final PsiBuilder.Marker arrayAccess = expr.precede();
				PsiBuilder.Marker argumentListMarker = builder.mark();
				builder.advanceLexer();

				parseArguments(builder, RBRACKET, false);

				if(builder.getTokenType() != RBRACKET)
				{
					builder.error("']' expected");
					argumentListMarker.done(CALL_ARGUMENT_LIST);
					arrayAccess.done(ARRAY_ACCESS_EXPRESSION);
					startMarker.drop();
					return arrayAccess;
				}
				builder.advanceLexer();
				argumentListMarker.done(CALL_ARGUMENT_LIST);

				arrayAccess.done(ARRAY_ACCESS_EXPRESSION);
				expr = arrayAccess;
			}
			else
			{
				startMarker.drop();
				return expr;
			}
		}
	}

	public static void parseArgumentList(CSharpBuilderWrapper builder, boolean fieldSet)
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

		parseArguments(builder, RPAR, fieldSet);

		expect(builder, RPAR, "')' expected");
		mark.done(CALL_ARGUMENT_LIST);
	}

	private static void parseArguments(CSharpBuilderWrapper builder, IElementType stopElement, boolean fieldSet)
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
			if(builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == COLON)
			{
				PsiBuilder.Marker marker = builder.mark();
				doneOneElement(builder, IDENTIFIER, REFERENCE_EXPRESSION, null);
				builder.advanceLexer(); // eq
				PsiBuilder.Marker expressionParser = parse(builder);
				if(expressionParser == null)
				{
					builder.error("Expression expected");
				}
				marker.done(NAMED_CALL_ARGUMENT);
			}
			else if(fieldSet && builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == EQ)
			{
				PsiBuilder.Marker marker = builder.mark();
				doneOneElement(builder, IDENTIFIER, REFERENCE_EXPRESSION, null);
				builder.advanceLexer(); // eq
				PsiBuilder.Marker expressionParser = parse(builder);
				if(expressionParser == null)
				{
					builder.error("Expression expected");
				}
				marker.done(FIELD_OR_PROPERTY_SET);
			}
			else
			{
				PsiBuilder.Marker argumentMarker = builder.mark();
				PsiBuilder.Marker marker = parse(builder);
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
	private static PsiBuilder.Marker parsePrimaryExpressionStart(final CSharpBuilderWrapper builder)
	{
		CSharpLanguageVersion version = builder.getVersion();
		builder.enableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);

		boolean linqState = false;
		if(version.isAtLeast(CSharpLanguageVersion._3_0))
		{
			linqState = builder.enableSoftKeyword(CSharpSoftTokens.FROM_KEYWORD);
		}
		if(version.isAtLeast(CSharpLanguageVersion._4_0))
		{
			builder.enableSoftKeyword(CSharpSoftTokens.AWAIT_KEYWORD);
			builder.enableSoftKeyword(CSharpSoftTokens.ASYNC_KEYWORD);
		}
		if(version.isAtLeast(CSharpLanguageVersion._6_0))
		{
			builder.enableSoftKeyword(CSharpSoftTokens.NAMEOF_KEYWORD);
		}
		IElementType tokenType = builder.getTokenType();
		if(linqState)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.FROM_KEYWORD);
		}
		if(version.isAtLeast(CSharpLanguageVersion._4_0))
		{
			builder.disableSoftKeyword(CSharpSoftTokens.AWAIT_KEYWORD);
			builder.disableSoftKeyword(CSharpSoftTokens.ASYNC_KEYWORD);
		}
		if(version.isAtLeast(CSharpLanguageVersion._6_0))
		{
			builder.disableSoftKeyword(CSharpSoftTokens.NAMEOF_KEYWORD);
		}

		builder.disableSoftKeyword(CSharpSoftTokens.GLOBAL_KEYWORD);

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
			boolean isLambdaNext = builder.getTokenType() == CSharpTokens.DELEGATE_KEYWORD || parseLambdaExpression(builder, null) != null;
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
			return parseNewExpression(builder, null);
		}

		if(tokenType == STACKALLOC_KEYWORD)
		{
			return parseStackAllocExpression(builder, null);
		}

		if(tokenType == TYPEOF_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, ALLOW_EMPTY_TYPE_ARGUMENTS, null, TYPE_OF_EXPRESSION);
		}

		if(tokenType == NAMEOF_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, NAMEOF_EXPRESSION);
		}

		if(tokenType == DEFAULT_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, NONE, null, DEFAULT_EXPRESSION);
		}

		if(tokenType == SIZEOF_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, NONE, null, SIZE_OF_EXPRESSION);
		}

		if(tokenType == CHECKED_KEYWORD || tokenType == UNCHECKED_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, CHECKED_EXPRESSION);
		}

		if(tokenType == __MAKEREF_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, __MAKEREF_EXPRESSION);
		}

		if(tokenType == __ARGLIST_KEYWORD)
		{
			return parseArglistExpression(builder);
		}

		if(tokenType == __REFTYPE_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, __REFTYPE_EXPRESSION);
		}

		if(tokenType == __REFVALUE_KEYWORD)
		{
			return parseRefValueExpression(builder);
		}

		if(tokenType == DELEGATE_KEYWORD)
		{
			return parseDelegateExpression(builder);
		}

		if(tokenType == REF_KEYWORD || tokenType == OUT_KEYWORD)
		{
			return parseOutRefWrapExpression(builder);
		}

		if(tokenType == AWAIT_KEYWORD)
		{
			return parseAwaitExpression(builder);
		}

		if(tokenType == ASYNC_KEYWORD)
		{
			if(builder.lookAhead(1) == CSharpTokens.DELEGATE_KEYWORD)
			{
				return parseDelegateExpression(builder);
			}
			return parseLambdaExpression(builder, null);
		}

		if(tokenType == LPAR)
		{
			final PsiBuilder.Marker lambda = parseLambdaAfterParenth(builder, null);
			if(lambda != null)
			{
				return lambda;
			}

			final PsiBuilder.Marker parenth = builder.mark();
			builder.advanceLexer();

			final PsiBuilder.Marker inner = parse(builder);
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
			PsiBuilder.Marker marker = LinqParsing.parseLinqExpression(builder);
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
			val refExpr = builder.mark();
			builder.advanceLexer();
			refExpr.done(REFERENCE_EXPRESSION);
			return refExpr;
		}

		if(tokenType == IDENTIFIER)
		{
			if(builder.lookAhead(1) == DARROW)
			{
				return parseLambdaExpression(builder, null);
			}

			val refExpr = builder.mark();

			builder.advanceLexer();
			parseReferenceTypeArgumentList(builder, NONE);
			refExpr.done(REFERENCE_EXPRESSION);
			return refExpr;
		}

		if(NATIVE_TYPES.contains(tokenType))
		{
			val refExpr = builder.mark();

			builder.advanceLexer();
			refExpr.done(REFERENCE_EXPRESSION);
			return refExpr;
		}

		if(THIS_OR_BASE.contains(tokenType))
		{
			val expr = builder.mark();
			builder.advanceLexer();
			expr.done(REFERENCE_EXPRESSION);
			return expr;
		}

		return null;
	}

	private static PsiBuilder.Marker parseArglistExpression(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			parseArgumentList(builder, false);
		}

		mark.done(__ARGLIST_EXPRESSION);
		return mark;
	}

	private static PsiBuilder.Marker parseRefValueExpression(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parse(builder) == null)
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

	private static PsiBuilder.Marker parseOutRefWrapExpression(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		if(builder.getTokenType() != IDENTIFIER)
		{
			builder.error("Identifier expected");
		}
		else
		{
			parse(builder);
		}
		mark.done(OUT_REF_WRAP_EXPRESSION);
		return mark;
	}

	private static PsiBuilder.Marker parseAwaitExpression(@NotNull CSharpBuilderWrapper builder)
	{
		val marker = builder.mark();
		builder.advanceLexer();

		if(parse(builder) == null)
		{
			builder.error("Expression expected");
		}

		marker.done(AWAIT_EXPRESSION);
		return marker;
	}

	private static PsiBuilder.Marker parseDelegateExpression(@NotNull CSharpBuilderWrapper builder)
	{
		val marker = builder.mark();

		if(builder.getTokenType() == CSharpSoftTokens.ASYNC_KEYWORD)
		{
			builder.advanceLexer();
		}
		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			MethodParsing.parseParameterList(builder, NONE, RPAR);
		}

		if(builder.getTokenType() == LBRACE)
		{
			StatementParsing.parse(builder);
		}
		else
		{
			builder.error("'{' expected");
		}

		marker.done(DELEGATE_EXPRESSION);
		return marker;
	}

	@Nullable
	private static PsiBuilder.Marker parseLambdaAfterParenth(final CSharpBuilderWrapper builder, @Nullable final PsiBuilder.Marker typeList)
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
					else if(tokenType == LPAR || tokenType == SEMICOLON ||
							tokenType == LBRACE || tokenType == RBRACE)
					{
						break;
					}
				}
				marker.rollbackTo();

				isLambda = arrow;
			}
		}

		return isLambda ? parseLambdaExpression(builder, typeList) : null;
	}

	@Nullable
	private static PsiBuilder.Marker parseLambdaExpression(final CSharpBuilderWrapper builder, @Nullable final PsiBuilder.Marker typeList)
	{
		val start = typeList != null ? typeList.precede() : builder.mark();

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
			body = StatementParsing.parse(builder);
		}
		else
		{
			body = parse(builder);
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
		val mark = builder.mark();

		boolean lpar = expect(builder, LPAR, null);

		if(!lpar || builder.getTokenType() != RPAR)
		{
			while(!builder.eof())
			{
				parseLambdaParameter(builder);

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else
				{
					break;
				}
			}
		}

		if(lpar)
		{
			expect(builder, RPAR, "')' expected");
		}

		mark.done(LAMBDA_PARAMETER_LIST);
	}

	private static void parseLambdaParameter(CSharpBuilderWrapper builder)
	{
		val mark = builder.mark();

		// typed
		if(MODIFIERS.contains(builder.getTokenType()))
		{
			parseModifierList(builder, NONE);

			if(parseType(builder) == null)
			{
				builder.error("Type expected");
			}
			else
			{
				expect(builder, IDENTIFIER, "Name expected");
			}
		}
		else
		{
			IElementType iElementType = builder.lookAhead(1);
			// not typed parameter
			if(builder.getTokenType() == IDENTIFIER && (iElementType == COMMA || iElementType == RPAR || iElementType == DARROW))
			{
				builder.advanceLexer();
			}
			else
			{
				if(parseType(builder) == null)
				{
					builder.error("Type expected");
				}
				else
				{
					expect(builder, IDENTIFIER, "Name expected");
				}
			}
		}

		mark.done(LAMBDA_PARAMETER);
	}

	private static void emptyExpression(final PsiBuilder builder)
	{
		emptyElement(builder, ERROR_EXPRESSION);
	}

	public static void parseConstructorSuperCall(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker marker = builder.mark();
		if(THIS_OR_BASE.contains(builder.getTokenType()))
		{
			doneOneElement(builder, builder.getTokenType(), REFERENCE_EXPRESSION, null);

			if(builder.getTokenType() == LPAR)
			{
				parseArgumentList(builder, false);
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
		PROPERTY_SET_LIST,
		ARRAY_INITIALZER,
		DICTIONARY_INITIALZER
	}

	private static PsiBuilder.Marker parseNewExpression(CSharpBuilderWrapper builder, PsiBuilder.Marker mark)
	{
		PsiBuilder.Marker newExpr = (mark != null ? mark.precede() : builder.mark());

		builder.advanceLexer();

		val typeInfo = parseType(builder, BRACKET_RETURN_BEFORE);

		boolean forceArray = false;

		while(parseArrayLength(builder))
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
			parseArgumentList(builder, false);
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
				parseFieldOrPropertySetBlock(builder);
				break;
			case DICTIONARY_INITIALZER:
				parseDictionaryInitializerList(builder);
				break;
			case ARRAY_INITIALZER:
				parseArrayInitializer(builder, ARRAY_INITIALIZER);
				break;
		}

		newExpr.done(NEW_EXPRESSION);
		return newExpr;
	}

	private static boolean parseArrayLength(@NotNull CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() == LBRACKET)
		{
			val arrayMarker = builder.mark();
			builder.advanceLexer();

			while(true)
			{
				parse(builder);
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

	private static PsiBuilder.Marker parseStackAllocExpression(CSharpBuilderWrapper builder, PsiBuilder.Marker mark)
	{
		PsiBuilder.Marker newExpr = (mark != null ? mark.precede() : builder.mark());

		builder.advanceLexer();

		val typeMarker = parseType(builder, BRACKET_RETURN_BEFORE);
		if(typeMarker == null)
		{
			builder.error("Expected type");
		}

		while(builder.getTokenType() == LBRACKET)
		{
			val arrayMarker = builder.mark();
			builder.advanceLexer();

			while(true)
			{
				parse(builder);
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

	@NotNull
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
			return AfterNewParsingTarget.PROPERTY_SET_LIST;
		}

		if(builderWrapper.lookAhead(1) == LBRACKET)
		{
			return AfterNewParsingTarget.DICTIONARY_INITIALZER;
		}

		if(builderWrapper.lookAhead(1) == IDENTIFIER && builderWrapper.lookAhead(2) == EQ)
		{
			return AfterNewParsingTarget.PROPERTY_SET_LIST;
		}
		else
		{
			return AfterNewParsingTarget.ARRAY_INITIALZER;
		}
	}

	private static PsiBuilder.Marker parseArrayInitializer(CSharpBuilderWrapper builderWrapper, IElementType to)
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
				parseArrayInitializerCompositeValue(builderWrapper);
				enteredValue = true;
			}
			else
			{
				PsiBuilder.Marker temp = builderWrapper.mark();
				if(parse(builderWrapper) != null)
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

	private static PsiBuilder.Marker parseArrayInitializerCompositeValue(CSharpBuilderWrapper builder)
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
			parseArguments(builder, RBRACE, false);

			expect(builder, RBRACE, "'}' expected");
		}

		headerMarker.done(ARRAY_INITIALIZER_COMPOSITE_VALUE);
		return headerMarker;
	}

	private static void parseFieldOrPropertySetBlock(CSharpBuilderWrapper builder)
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

				if(parseFieldOrPropertySet(builder) == null)
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

	private static PsiBuilder.Marker parseFieldOrPropertySet(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(doneOneElement(builder, IDENTIFIER, REFERENCE_EXPRESSION, "Identifier expected"))
		{
			if(expect(builder, EQ, "'=' expected"))
			{
				if(parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}
			mark.done(FIELD_OR_PROPERTY_SET);
			return mark;
		}
		else
		{
			mark.rollbackTo();
			return null;
		}
	}

	private static void parseDictionaryInitializerList(CSharpBuilderWrapper builder)
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

				if(parseDictionaryInitializer(builder) == null)
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

	private static PsiBuilder.Marker parseDictionaryInitializer(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		if(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker firstArgumentMarker = builder.mark();
			builder.advanceLexer();
			if(parse(builder) == null)
			{
				builder.error("Expression expected");
			}
			expect(builder, RBRACKET, "']' expected");
			firstArgumentMarker.done(CALL_ARGUMENT);

			if(expect(builder, EQ, "'=' expected"))
			{
				PsiBuilder.Marker valueArgumentMarker = builder.mark();
				if(parse(builder) == null)
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

	private static PsiBuilder.Marker parseExpressionWithTypeInLParRPar(CSharpBuilderWrapper builder, int flags, PsiBuilder.Marker mark,
			IElementType to)
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

	private static PsiBuilder.Marker parseExpressionWithExpressionInLParRPar(CSharpBuilderWrapper builder, PsiBuilder.Marker mark, IElementType to)
	{
		PsiBuilder.Marker newMarker = mark == null ? builder.mark() : mark.precede();
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parse(builder) == null)
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

	public static ReferenceInfo parseQualifiedReference(@NotNull CSharpBuilderWrapper builder, @Nullable PsiBuilder.Marker prevMarker)
	{
		return parseQualifiedReference(builder, prevMarker, NONE, TokenSet.EMPTY);
	}

	public static ReferenceInfo parseQualifiedReference(@NotNull CSharpBuilderWrapper builder,
			@Nullable final PsiBuilder.Marker prevMarker,
			int flags,
			@NotNull TokenSet nameStopperSet)
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

				marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.REFERENCE_EXPRESSION : CSharpElements.REFERENCE_EXPRESSION);

				return parseQualifiedReference(builder, marker.precede(), flags, nameStopperSet);
			}
			else
			{
				builder.remapBackIfSoft();
			}
		}

		if(expect(builder, IDENTIFIER, "Identifier expected"))
		{
			referenceInfo.isParameterized = parseReferenceTypeArgumentList(builder, flags) != null;

			marker.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.REFERENCE_EXPRESSION : CSharpElements.REFERENCE_EXPRESSION);

			if(builder.getTokenType() == DOT)
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
	private static PsiBuilder.Marker parseReferenceTypeArgumentList(@NotNull CSharpBuilderWrapper builder, int flags)
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
	private static PsiBuilder.Marker parseReferenceEmptyTypeArgumentListImpl(@NotNull CSharpBuilderWrapper builder)
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
	private static PsiBuilder.Marker parseReferenceTypeArgumentListImpl(@NotNull CSharpBuilderWrapper builder, int flags)
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
				val marker = parseType(builder, flags);
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
}
