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

package consulo.csharp.lang.parser.decl;

import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.BitUtil;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.parser.exp.ExpressionParsing;
import consulo.csharp.lang.parser.stmt.StatementParsing;
import consulo.csharp.lang.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTokens;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

	public static void parseMethodStartAtType(@Nonnull CSharpBuilderWrapper builder, @Nonnull PsiBuilder.Marker marker, @Nonnull ModifierSet set)
	{
		TypeInfo typeInfo = parseType(builder, STUB_SUPPORT);
		if(typeInfo != null)
		{
			parseMethodStartAfterType(builder, marker, typeInfo, Target.METHOD, set);
		}
		else
		{
			builder.error("Name expected");
			done(marker, METHOD_DECLARATION);
		}
	}

	public static void parseMethodStartAfterType(@Nonnull CSharpBuilderWrapper builder,
												 @Nonnull PsiBuilder.Marker marker,
												 @Nullable TypeInfo typeInfo,
												 @Nonnull Target target,
												 @Nonnull ModifierSet set)
	{
		if(target == Target.CONSTRUCTOR || target == Target.DECONSTRUCTOR)
		{
			expectOrReportIdentifier(builder, STUB_SUPPORT);
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
				expectOrReportIdentifier(builder, STUB_SUPPORT);
			}
		}

		parseMethodStartAfterName(builder, marker, target, set);
	}

	public static void parseMethodStartAfterName(@Nonnull CSharpBuilderWrapper builder, @Nonnull PsiBuilder.Marker marker, @Nonnull Target target, @Nonnull ModifierSet set)
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
				parseParameterList(builder, STUB_SUPPORT, RPAR, set);
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

				ExpressionParsing.parseConstructorSuperCall(builder, set, 0);
			}
		}
		else if(target != Target.DECONSTRUCTOR)
		{
			GenericParameterParsing.parseGenericConstraintList(builder);
		}

		parseMethodBodyOrSemicolon(builder, set);

		switch(target)
		{
			case DECONSTRUCTOR:
			case CONSTRUCTOR:
				done(marker, CONSTRUCTOR_DECLARATION);
				break;
			case METHOD:
				done(marker, METHOD_DECLARATION);
				break;
			case CONVERSION_METHOD:
				done(marker, CONVERSION_METHOD_DECLARATION);
				break;
		}
	}

	private static void parseMethodBoryOrSemicolon(CSharpBuilderWrapper builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == LBRACE)
		{
			PsiBuilder.Marker marker = builder.mark();

			int braceLevel = 0;

			while(!builder.eof())
			{
				IElementType nextToken = builder.getTokenType();
				if(nextToken == LBRACE)
				{
					braceLevel++;
				}
				else if(nextToken == RBRACE)
				{
					braceLevel--;

					if(braceLevel <= 0)
					{
						// eat only if level correct
						// least error brace for next item parsing
						if(braceLevel == 0)
						{
							builder.advanceLexer();
						}
						break;
					}
				}

				builder.advanceLexer();
			}

			marker.collapse(CSharpElements.STATEMENT_METHOD_BODY);
		}
		else if(tokenType == DARROW)
		{
			PsiBuilder.Marker marker = builder.mark();

			int braceLevel = 0;

			while(!builder.eof())
			{
				IElementType nextToken = builder.getTokenType();
				if(nextToken == LBRACE)
				{
					braceLevel++;
				}
				else if(nextToken == RBRACE)
				{
					braceLevel--;
				}
				else if(nextToken == SEMICOLON && braceLevel <= 0)
				{
					builder.advanceLexer();
					break;
				}

				builder.advanceLexer();
			}

			marker.collapse(CSharpElements.EXPRESSION_METHOD_BODY);
		}
		else if(tokenType == SEMICOLON)
		{
			builder.advanceLexer();
		}
		else
		{
			builder.error("';' expected");
		}
	}

	@Deprecated
	public static void parseMethodBodyOrSemicolon(CSharpBuilderWrapper builder, ModifierSet set)
	{
		parseMethodBoryOrSemicolon(builder);
	}

	public static boolean parseMethodBody(CSharpBuilderWrapper builder, ModifierSet set)
	{
		if(builder.getTokenType() == LBRACE)
		{
			StatementParsing.parse(builder, set);
			return true;
		}
		else if(builder.getTokenType() == DARROW)
		{
			builder.advanceLexer();
			ExpressionParsing.parse(builder, set);
			expect(builder, SEMICOLON, "';' expected");
			return true;
		}
		else
		{
			return false;
		}
	}

	public static void parseParameterList(CSharpBuilderWrapper builder, int flags, IElementType end, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() != end)
		{
			while(!builder.eof())
			{
				parseParameter(builder, end, flags, set);

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

	private static void parseParameter(CSharpBuilderWrapper builder, IElementType end, int flags, ModifierSet set)
	{
		PsiBuilder.Marker mark = builder.mark();

		Pair<PsiBuilder.Marker, ModifierSet> modifierPair = parseModifierListWithAttributes(builder, flags);

		TypeInfo typeInfo = parseType(builder, flags);
		if(typeInfo == null)
		{
			if(modifierPair.getSecond().isEmpty())
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
			if(typeInfo.nativeElementType != CSharpTokens.__ARGLIST_KEYWORD)
			{
				expectOrReportIdentifier(builder, flags);

				if(expect(builder, EQ, null))
				{
					if(ExpressionParsing.parse(builder, set) == null)
					{
						builder.error("Expression expected.");
					}
				}
			}
			mark.done(BitUtil.isSet(flags, STUB_SUPPORT) ? CSharpStubElements.PARAMETER : CSharpElements.PARAMETER);
		}
	}
}
