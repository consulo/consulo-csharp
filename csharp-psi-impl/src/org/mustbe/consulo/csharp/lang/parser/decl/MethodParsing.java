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
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.BitUtil;
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
		DECONSTRUCTOR,
		METHOD,
		CONVERSION_METHOD
	}

	public static void parseMethodStartAtType(@NotNull CSharpBuilderWrapper builder, @NotNull PsiBuilder.Marker marker)
	{
		TypeInfo typeInfo = parseType(builder, STUB_SUPPORT);
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

	public static void parseMethodStartAfterType(@NotNull CSharpBuilderWrapper builder,
			@NotNull PsiBuilder.Marker marker,
			@Nullable TypeInfo typeInfo,
			@NotNull Target target)
	{
		if(target == Target.CONSTRUCTOR || target == Target.DECONSTRUCTOR)
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
					if(parseType(builder, STUB_SUPPORT) == null)
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
			// deconstructors dont process any parameters
			if(target == Target.DECONSTRUCTOR)
			{
				PsiBuilder.Marker parameterMarker = builder.mark();
				builder.advanceLexer();
				expect(builder, RPAR, "')' expected");
				parameterMarker.done(CSharpStubElements.PARAMETER_LIST);
			}
			else
			{
				parseParameterList(builder, STUB_SUPPORT, RPAR);
			}
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
		else if(target != Target.DECONSTRUCTOR)
		{
			GenericParameterParsing.parseGenericConstraintList(builder);
		}

		if(!expect(builder, SEMICOLON, null))
		{
			if(builder.getTokenType() == LBRACE)
			{
				StatementParsing.parse(builder);
			}
			else if(builder.getTokenType() == DARROW)
			{
				builder.advanceLexer();
				ExpressionParsing.parse(builder);
				expect(builder, SEMICOLON, "';' expected");
			}
			else
			{
				builder.error("';' expected");
			}
		}

		switch(target)
		{
			case DECONSTRUCTOR:
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

	public static void parseParameterList(CSharpBuilderWrapper builder, int flags, IElementType end)
	{
		val mark = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() != end)
		{
			while(!builder.eof())
			{
				parseParameter(builder, end, flags);

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() == end)
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

		expect(builder, end, "')' expected");
		mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.PARAMETER_LIST : CSharpElements.PARAMETER_LIST);
	}

	private static void parseParameter(CSharpBuilderWrapper builder, IElementType end, int flags)
	{
		val mark = builder.mark();

		Pair<PsiBuilder.Marker, Boolean> modifierPair = parseModifierListWithAttributes(builder, flags);

		if(parseType(builder, flags) == null)
		{
			if(modifierPair.getSecond() == Boolean.TRUE)
			{
				// if no modifiers but we failed parse type - need go advance, due ill infinity loop inside parseParameterList,
				// but dont eat close brace
				if(builder.getTokenType() != end)
				{
					builder.advanceLexer();
				}
			}
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
			mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.PARAMETER : CSharpElements.PARAMETER);
		}
	}
}
