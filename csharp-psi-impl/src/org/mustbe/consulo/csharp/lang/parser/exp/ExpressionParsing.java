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
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.decl.MethodParsing;
import org.mustbe.consulo.csharp.lang.parser.stmt.StatementParsing;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import lombok.val;

public class ExpressionParsing extends SharingParsingHelpers
{
	private enum ExprType
	{
		CONDITIONAL_OR, CONDITIONAL_AND, OR, XOR, AND, EQUALITY, RELATIONAL, SHIFT, ADDITIVE, MULTIPLICATIVE, UNARY, TYPE
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
	private static final TokenSet PREF_ARITHMETIC_OPS = TokenSet.orSet(POSTFIX_OPS, TokenSet.create(PLUS, MINUS));
	private static final TokenSet PREFIX_OPS = TokenSet.orSet(PREF_ARITHMETIC_OPS, TokenSet.create(TILDE, EXCL));
	private static final TokenSet ARGS_LIST_CONTINUE = TokenSet.create(IDENTIFIER, TokenType.BAD_CHARACTER, COMMA, INTEGER_LITERAL, STRING_LITERAL);
	private static final TokenSet ARGS_LIST_END = TokenSet.create(RPAR, RBRACE, RBRACKET);
	private static final TokenSet ID_OR_SUPER = TokenSet.create(IDENTIFIER, BASE_KEYWORD);
	private static final TokenSet THIS_OR_BASE = TokenSet.create(THIS_KEYWORD, BASE_KEYWORD);


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
	public static PsiBuilder.Marker parseConditional(final CSharpBuilderWrapper builder)
	{
		final PsiBuilder.Marker condition = parseExpression(builder, ExprType.CONDITIONAL_OR);
		if(condition == null)
		{
			return null;
		}

		if(builder.getTokenType() == QUEST)
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
		else if(builder.getTokenType() == NULL_COALESCING)
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
		else
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

			case TYPE:
				TypeInfo typeInfo = parseType(builder, BracketFailPolicy.NOTHING, false);
				return typeInfo == null ? null : typeInfo.marker;
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

			val typeInfo = parseType(builder, BracketFailPolicy.NOTHING, false);
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
		PsiBuilder.Marker operand = parsePrimary(builder, null, -1);
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
		if(ops.contains(tokenType))
		{
			// save
			result = result.precede();

			PsiBuilder.Marker operatorMarker = builder.mark();
			builder.advanceLexerGGLL();
			operatorMarker.done(OPERATOR_REFERENCE);

			PsiBuilder.Marker right = parse(builder);
			if(right == null)
			{
				builder.error("Expression expected");
			}

			result.done(BINARY_EXPRESSION);
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
				toParse = ExprType.TYPE;
			}
			else if(tokenType == AS_KEYWORD)
			{
				toCreate = AS_EXPRESSION;
				toParse = ExprType.TYPE;
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
				builder.error(toParse == ExprType.TYPE ? "Type expected" : "Expression expected");
				expression.done(toCreate);
				return expression;
			}

			expression.done(toCreate);
			left = expression;
		}

		return left;
	}

	private static enum BreakPoint
	{
		P1, P2, P3, P4
	}

	@Nullable
	private static PsiBuilder.Marker parsePrimary(final CSharpBuilderWrapper builder, @Nullable final BreakPoint breakPoint, final int breakOffset)
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
			if(tokenType == DOT)
			{
				final PsiBuilder.Marker dotPos = builder.mark();
				final int dotOffset = builder.getCurrentOffset();
				builder.advanceLexer();

				IElementType dotTokenType = builder.getTokenType();

				if(dotTokenType == NEW_KEYWORD)
				{
					dotPos.drop();
					expr = parseNewExpression(builder, expr);
				}
			/*	else if(THIS_OR_SUPER.contains(dotTokenType) && exprType(expr) == REFERENCE_EXPRESSION)
				{
					if(breakPoint == BreakPoint.P2 && builder.getCurrentOffset() == breakOffset)
					{
						dotPos.rollbackTo();
						startMarker.drop();
						return expr;
					}

					final PsiBuilder.Marker copy = startMarker.precede();
					final int offset = builder.getCurrentOffset();
					startMarker.rollbackTo();

					final PsiBuilder.Marker ref = myParser.getReferenceParser().parseJavaCodeReference(builder, false, true, false, false);
					if(ref == null || builder.getTokenType() != DOT || builder.getCurrentOffset() != dotOffset)
					{
						copy.rollbackTo();
						return parsePrimary(builder, BreakPoint.P2, offset);
					}
					builder.advanceLexer();

					if(builder.getTokenType() != dotTokenType)
					{
						copy.rollbackTo();
						return parsePrimary(builder, BreakPoint.P2, offset);
					}
					builder.advanceLexer();

					startMarker = copy;
					expr = ref.precede();
					expr.done(dotTokenType == THIS_KEYWORD ? THIS_EXPRESSION : SUPER_EXPRESSION);
				} */
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
					//myParser.getReferenceParser().parseReferenceParameterList(builder, false, false);

					if(!expect(builder, ID_OR_SUPER, "expected.identifier"))
					{
						refExpr.done(REFERENCE_EXPRESSION);
						startMarker.drop();
						return refExpr;
					}

					refExpr.done(REFERENCE_EXPRESSION);
					expr = refExpr;
				}
			}
			else if(tokenType == LT)
			{
				if(exprType(expr) != REFERENCE_EXPRESSION)
				{
					startMarker.drop();
					return expr;
				}

				final PsiBuilder.Marker callExpr = expr.precede();

				PsiBuilder.Marker argumentMark = builder.mark();
				builder.advanceLexer();
				parseTypeList(builder, false);
				expect(builder, GT, "'>' expected");
				argumentMark.done(TYPE_ARGUMENTS);

				if(builder.getTokenType() == LPAR)
				{
					parseArgumentList(builder);
					callExpr.done(METHOD_CALL_EXPRESSION);
					expr = callExpr;
				}
				else
				{
					argumentMark.rollbackTo();
					callExpr.drop();
					startMarker.drop();
					return expr;
				}
			}
			else if(tokenType == LPAR)
			{
				if(exprType(expr) != REFERENCE_EXPRESSION)
				{
					startMarker.drop();
					return expr;
				}

				final PsiBuilder.Marker callExpr = expr.precede();
				parseArgumentList(builder);
				callExpr.done(METHOD_CALL_EXPRESSION);
				expr = callExpr;
			}
			else if(tokenType == LBRACKET)
			{
				if(breakPoint == BreakPoint.P4)
				{
					startMarker.drop();
					return expr;
				}

				final PsiBuilder.Marker arrayAccess = expr.precede();
				PsiBuilder.Marker argumentListMarker = builder.mark();
				builder.advanceLexer();

				while(true)
				{
					final PsiBuilder.Marker index = parse(builder);
					if(index == null)
					{
						builder.error("Expression expected");
					}

					if(builder.getTokenType() != COMMA)
					{
						break;
					}
					else
					{
						builder.advanceLexer();
					}
				}

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

	@NotNull
	public static PsiBuilder.Marker parseArgumentList(final CSharpBuilderWrapper builder)
	{
		final PsiBuilder.Marker list = builder.mark();
		builder.advanceLexer();

		boolean first = true;
		while(true)
		{
			final IElementType tokenType = builder.getTokenType();
			if(first && (ARGS_LIST_END.contains(tokenType) || builder.eof()))
			{
				break;
			}
			if(!first && !ARGS_LIST_CONTINUE.contains(tokenType))
			{
				break;
			}

			boolean hasError = false;
			if(!first)
			{
				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else
				{
					hasError = true;
					builder.error("Expected ',' or ')'");
					emptyExpression(builder);
				}
			}
			first = false;

			final PsiBuilder.Marker arg = parse(builder);
			if(arg == null)
			{
				if(!hasError)
				{
					builder.error("Expression expected");
					emptyExpression(builder);
				}
				if(!ARGS_LIST_CONTINUE.contains(builder.getTokenType()))
				{
					break;
				}
				if(builder.getTokenType() != COMMA && !builder.eof())
				{
					builder.advanceLexer();
				}
			}
		}

		final boolean closed = expect(builder, RPAR, "expected.rparen");

		list.done(CALL_ARGUMENT_LIST);
		if(!closed)
		{
			list.setCustomEdgeTokenBinders(null, WhitespacesBinders.DEFAULT_LEFT_BINDER);
		}
		return list;
	}

	@Nullable
	private static PsiBuilder.Marker parsePrimaryExpressionStart(final CSharpBuilderWrapper builder)
	{
		IElementType tokenType = builder.getTokenType();

		if(LITERALS.contains(tokenType))
		{
			final PsiBuilder.Marker literal = builder.mark();
			builder.advanceLexer();
			literal.done(CONSTANT_EXPRESSION);
			return literal;
		}

		if(tokenType == LBRACE)
		{
			return parseArrayInitialization(builder);
		}

		if(tokenType == NEW_KEYWORD)
		{
			return parseNewExpression(builder, null);
		}

		if(tokenType == TYPEOF_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, null, TYPE_OF_EXPRESSION);
		}

		if(tokenType == DEFAULT_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, null, DEFAULT_EXPRESSION);
		}

		if(tokenType == SIZEOF_KEYWORD)
		{
			return parseExpressionWithTypeInLParRPar(builder, null, SIZE_OF_EXPRESSION);
		}

		if(tokenType == CHECKED_KEYWORD || tokenType == UNCHECKED_KEYWORD)
		{
			return parseExpressionWithExpressionInLParRPar(builder, null, CHECKED_EXPRESSION);
		}

		if(tokenType == DELEGATE_KEYWORD)
		{
			return parseAnonymMethodExpression(builder, null);
		}

		if(tokenType == REF_KEYWORD || tokenType == OUT_KEYWORD)
		{
			return parseOutRefWrapExpression(builder);
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
			return LinqParsing.parseLinqExpression(builder);
		}

		if(tokenType == IDENTIFIER)
		{
			if(builder.lookAhead(1) == DARROW)
			{
				return parseLambdaExpression(builder, null);
			}

			val refExpr = builder.mark();

			builder.advanceLexer();
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

	private static PsiBuilder.Marker parseAnonymMethodExpression(@NotNull CSharpBuilderWrapper builder, final PsiBuilder.Marker m)
	{
		val marker = m == null ? builder.mark() : m;
		builder.advanceLexer();

		if(builder.getTokenType() == LPAR)
		{
			MethodParsing.parseParameterList(builder, RPAR);
		}

		if(builder.getTokenType() == LBRACE)
		{
			StatementParsing.parse(builder);
		}
		else
		{
			builder.error("'{' expected");
		}

		marker.done(ANONYM_METHOD_EXPRESSION);
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
			parseModifierList(builder);

			if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
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
				if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
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
		emptyElement(builder, EMPTY_EXPRESSION);
	}

	public static void parseConstructorSuperCall(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker marker = builder.mark();
		if(THIS_OR_BASE.contains(builder.getTokenType()))
		{
			doneOneElement(builder, builder.getTokenType(), REFERENCE_EXPRESSION, null);

			if(builder.getTokenType() == LPAR)
			{
				parseArgumentList(builder);
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

	public static void parseParameterList(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() == RPAR)
		{
			builder.advanceLexer();
			mark.done(CALL_ARGUMENT_LIST);
			return;
		}

		boolean empty = true;
		while(!builder.eof())
		{
			PsiBuilder.Marker marker = parse(builder);
			if(marker == null)
			{
				if(!empty)
				{
					builder.error("Expression expected");
				}
				break;
			}

			empty = false;

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else if(builder.getTokenType() == RPAR)
			{
				break;
			}
			else
			{
				break;
			}
		}
		expect(builder, RPAR, "')' expected");
		mark.done(CALL_ARGUMENT_LIST);
	}

	private static enum AfterNewParsingTarget
	{
		NONE,
		PROPERTY_SET_LIST,
		ARRAY_INITIALIZATION
	}

	private static PsiBuilder.Marker parseNewExpression(CSharpBuilderWrapper builder, PsiBuilder.Marker mark)
	{
		PsiBuilder.Marker newExpr = (mark != null ? mark.precede() : builder.mark());

		builder.advanceLexer();

		val typeMarker = parseType(builder, BracketFailPolicy.RETURN_BEFORE, false);
		if(typeMarker != null)
		{
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

			if(builder.getTokenType() == LPAR)
			{
				parseArgumentList(builder);
			}

			AfterNewParsingTarget target = getTarget(builder);
			switch(target)
			{
				case NONE:
					break;
				case PROPERTY_SET_LIST:
					parseFieldOrPropertySetBlock(builder);
					break;
				case ARRAY_INITIALIZATION:
					parseArrayInitialization(builder);
					break;
			}
		}
		else
		{
			if(builder.getTokenType() == LBRACE)
			{
				parseFieldOrPropertySetBlock(builder);
			}
			else
			{
				builder.error("'{' expected");
			}
		}

		newExpr.done(NEW_EXPRESSION);
		return newExpr;
	}

	private static AfterNewParsingTarget getTarget(CSharpBuilderWrapper builderWrapper)
	{
		if(builderWrapper.getTokenType() != LBRACE)
		{
			return AfterNewParsingTarget.NONE;
		}

		if(builderWrapper.lookAhead(1) == IDENTIFIER && builderWrapper.lookAhead(2) == EQ)
		{
			return AfterNewParsingTarget.PROPERTY_SET_LIST;
		}
		else
		{
			return AfterNewParsingTarget.ARRAY_INITIALIZATION;
		}
	}

	private static PsiBuilder.Marker parseArrayInitialization(CSharpBuilderWrapper builderWrapper)
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

			PsiBuilder.Marker parse = ExpressionParsing.parse(builderWrapper);
			if(parse == null)
			{
				builderWrapper.error("Expression expected");
			}

			if(builderWrapper.getTokenType() == COMMA)
			{
				builderWrapper.advanceLexer();
			}
			else
			{
				break;
			}
		}

		expect(builderWrapper, RBRACE, "'}' expected");
		marker.done(ARRAY_INITIALIZATION_EXPRESSION);
		return marker;
	}

	private static void parseFieldOrPropertySetBlock(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(!expect(builder, RBRACE, null))
		{
			while(!builder.eof())
			{
				if(parseFieldOrPropertySet(builder) == null)
				{
					break;
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
				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}
			mark.done(FIELD_OR_PROPERTY_SET);
			return mark;
		}
		else
		{
			mark.drop();
			return null;
		}
	}

	private static PsiBuilder.Marker parseExpressionWithTypeInLParRPar(CSharpBuilderWrapper builder, PsiBuilder.Marker mark, IElementType to)
	{
		PsiBuilder.Marker newMarker = mark == null ? builder.mark() : mark.precede();
		builder.advanceLexer();

		if(expect(builder, LPAR, "'(' expected"))
		{
			if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
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
				builder.error("Type expected");
			}
			expect(builder, RPAR, "')' expected");
		}
		newMarker.done(to);
		return newMarker;
	}

	public static PsiBuilder.Marker parseQualifiedReference(@NotNull PsiBuilder builder, @Nullable PsiBuilder.Marker prevMarker)
	{
		return parseQualifiedReference(builder, prevMarker, TokenSet.EMPTY);
	}

	public static PsiBuilder.Marker parseQualifiedReference(@NotNull PsiBuilder builder, @Nullable PsiBuilder.Marker prevMarker,
			TokenSet nameStopperSet)
	{
		if(prevMarker != null)
		{
			builder.advanceLexer(); // skip dot
		}
		PsiBuilder.Marker marker = prevMarker == null ? builder.mark() : prevMarker;

		if(expect(builder, IDENTIFIER, "Identifier expected"))
		{
			marker.done(REFERENCE_EXPRESSION);

			if(builder.getTokenType() == DOT)
			{
				// if after dot we found stoppers, name expected - but we done
				if(nameStopperSet.contains(builder.lookAhead(1)) || nameStopperSet.contains(builder.lookAhead(2)))
				{
					return marker;
				}
				marker = parseQualifiedReference(builder, marker.precede(), nameStopperSet);
			}
		}
		else
		{
			marker.drop();
			marker = null;
		}

		return marker;
	}
}
