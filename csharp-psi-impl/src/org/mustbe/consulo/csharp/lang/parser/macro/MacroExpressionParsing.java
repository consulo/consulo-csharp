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

package org.mustbe.consulo.csharp.lang.parser.macro;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import lombok.val;

/**
 * @author VISTALL
 * @since 16.12.13
 *        <p/>
 *        Base on code from java plugin - class ExpressionParser
 *        License Apache 2, Copyright 2000-2013 JetBrains s.r.o
 */
public class MacroExpressionParsing implements CSharpMacroTokens, CSharpMacroElements
{
	private enum ExprType
	{
		CONDITIONAL_OR, CONDITIONAL_AND,  UNARY
	}

	private static final TokenSet CONDITIONAL_OR_OPS = TokenSet.create(OROR);
	private static final TokenSet CONDITIONAL_AND_OPS = TokenSet.create(ANDAND);
	private static final TokenSet PREFIX_OPS = TokenSet.create(EXCL);


	@Nullable
	public static PsiBuilder.Marker parse(final PsiBuilder builder)
	{
		return parseExpression(builder, ExprType.CONDITIONAL_OR);
	}

	@Nullable
	private static PsiBuilder.Marker parseExpression(final PsiBuilder builder, final ExprType type)
	{
		switch(type)
		{
			case CONDITIONAL_OR:
				return parseBinary(builder, ExprType.CONDITIONAL_AND, CONDITIONAL_OR_OPS);

			case CONDITIONAL_AND:
				return parseBinary(builder, ExprType.UNARY, CONDITIONAL_AND_OPS);

			case UNARY:
				return parseUnary(builder);

			default:
				assert false : "Unexpected type: " + type;
				return null;
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseUnary(final PsiBuilder builder)
	{
		final IElementType tokenType = builder.getTokenType();

		if(PREFIX_OPS.contains(tokenType))
		{
			final PsiBuilder.Marker unary = builder.mark();
			builder.advanceLexer();

			final PsiBuilder.Marker operand = parseUnary(builder);
			if(operand == null)
			{
				builder.error("Expression expected");
			}

			unary.done(PREFIX_EXPRESSION);
			return unary;
		}
		else
		{
			return parsePrimary(builder);
		}
	}

	@Nullable
	private static PsiBuilder.Marker parseBinary(final PsiBuilder builder, final ExprType type, final TokenSet ops)
	{
		PsiBuilder.Marker result = parseExpression(builder, type);
		if(result == null)
		{
			return null;
		}
		int operandCount = 1;

		IElementType tokenType = builder.getTokenType();
		IElementType currentExprTokenType = tokenType;
		while(true)
		{
			if(tokenType == null || !ops.contains(tokenType))
			{
				break;
			}

			builder.advanceLexer();

			final PsiBuilder.Marker right = parseExpression(builder, type);
			operandCount++;
			tokenType = builder.getTokenType();
			if(tokenType == null || !ops.contains(tokenType) || tokenType != currentExprTokenType || right == null)
			{
				// save
				result = result.precede();
				if(right == null)
				{
					builder.error("Expression expected");
				}
				result.done(operandCount > 2 ? POLYADIC_EXPRESSION : BINARY_EXPRESSION);
				if(right == null)
				{
					break;
				}
				currentExprTokenType = tokenType;
				operandCount = 1;
			}
		}

		return result;
	}

	@Nullable
	private static PsiBuilder.Marker parsePrimary(final PsiBuilder builder)
	{
		PsiBuilder.Marker startMarker = builder.mark();

		PsiBuilder.Marker expr = parsePrimaryExpressionStart(builder);
		if(expr == null)
		{
			startMarker.drop();
			return null;
		}
		startMarker.drop();
		return expr;
	}

	@Nullable
	private static PsiBuilder.Marker parsePrimaryExpressionStart(final PsiBuilder builder)
	{
		IElementType tokenType = builder.getTokenType();


		if(tokenType == IDENTIFIER)
		{
			val refExpr = builder.mark();

			builder.advanceLexer();
			refExpr.done(REFERENCE_EXPRESSION);
			return refExpr;
		}

		if(tokenType == LPAR)
		{
			final PsiBuilder.Marker parenth = builder.mark();
			builder.advanceLexer();

			final PsiBuilder.Marker inner = parse(builder);
			if(inner == null)
			{
				builder.error("Expression expected");
			}

			if(!SharingParsingHelpers.expect(builder, RPAR, null))
			{
				if(inner != null)
				{
					builder.error("')' expected");
				}
			}

			parenth.done(PARENTHESES_EXPRESSION);
			return parenth;
		}
		return null;
	}
}
