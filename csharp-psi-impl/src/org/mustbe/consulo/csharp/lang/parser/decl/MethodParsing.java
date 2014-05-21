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

package org.mustbe.consulo.csharp.lang.parser.decl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.parser.stmt.StatementParsing;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class MethodParsing extends MemberWithBodyParsing
{
	public static enum Target
	{
		CONSTRUCTOR,
		METHOD,
		CONVERSION_METHOD
	}

	public static void parseMethodStartAtType(@NotNull CSharpBuilderWrapper builder, @NotNull PsiBuilder.Marker marker)
	{
		TypeInfo typeInfo = parseType(builder, BracketFailPolicy.NOTHING, false);
		if(typeInfo != null)
		{
			parseMethodStartAfterType(builder, marker, typeInfo, Target.METHOD);
		}
		else
		{
			builder.error("Name expected");
			marker.done(METHOD_DECLARATION);
		}
	}

	public static void parseMethodStartAfterType(@NotNull CSharpBuilderWrapper builder, @NotNull PsiBuilder.Marker marker,
			@Nullable TypeInfo typeInfo, @NotNull Target target)
	{
		if(target == Target.CONSTRUCTOR)
		{
			expect(builder, IDENTIFIER, "Name expected");
		}
		else
		{
			if(builder.getTokenType() == OPERATOR_KEYWORD)
			{
				builder.advanceLexer();

				IElementType tokenTypeGGLL = builder.getTokenTypeGGLL();

				if(typeInfo != null && (typeInfo.nativeElementType == EXPLICIT_KEYWORD || typeInfo.nativeElementType == IMPLICIT_KEYWORD))
				{
					if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
					{
						builder.error("Type expected");
					}

					target = Target.CONVERSION_METHOD;
				}
				else
				{
					if(OVERLOADING_OPERATORS.contains(tokenTypeGGLL))
					{
						builder.advanceLexerGGLL();
					}
					else
					{
						builder.error("Operator name expected");
					}
				}
			}
			else
			{
				expect(builder, IDENTIFIER, "Name expected");
			}
		}

		parseMethodStartAfterName(builder, marker, target);
	}

	public static void parseMethodStartAfterName(@NotNull CSharpBuilderWrapper builder, @NotNull PsiBuilder.Marker marker, @NotNull Target target)
	{
		GenericParameterParsing.parseList(builder);

		if(builder.getTokenType() == LPAR)
		{
			parseParameterList(builder, RPAR);
		}
		else
		{
			builder.error("'(' expected");
		}

		if(target == Target.CONSTRUCTOR)
		{
			if(builder.getTokenType() == COLON)
			{
				builder.advanceLexer();

				ExpressionParsing.parseConstructorSuperCall(builder);
			}
		}
		else
		{
			GenericParameterParsing.parseGenericConstraintList(builder);
		}

		if(!expect(builder, SEMICOLON, null))
		{
			if(builder.getTokenType() == LBRACE)
			{
				StatementParsing.parse(builder);
			}
			else
			{
				builder.error("';' expected");
			}
		}

		switch(target)
		{
			case CONSTRUCTOR:
				marker.done(CONSTRUCTOR_DECLARATION);
				break;
			case METHOD:
				marker.done(METHOD_DECLARATION);
				break;
			case CONVERSION_METHOD:
				marker.done(CONVERSION_METHOD_DECLARATION);
				break;
		}
	}

	public static void parseParameterList(CSharpBuilderWrapper builder, IElementType end)
	{
		val mark = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() != end)
		{
			while(!builder.eof())
			{
				parseParameter(builder);

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

		expect(builder, end, "')' expected");
		mark.done(PARAMETER_LIST);
	}

	private static void parseParameter(CSharpBuilderWrapper builder)
	{
		val mark = builder.mark();

		parseModifierListWithAttributes(builder);

		if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
		{
			mark.error("Type expected");
		}
		else
		{
			expect(builder, IDENTIFIER, "Name expected");

			if(expect(builder, EQ, null))
			{
				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected.");
				}
			}
			mark.done(PARAMETER);
		}
	}
}
