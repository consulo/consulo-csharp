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
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.util.NotNullFunction;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class TypeDeclarationParsing extends SharingParsingHelpers
{
	public static void parse(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		boolean isEnum = builder.getTokenType() == ENUM_KEYWORD;

		builder.advanceLexer();

		expect(builder, IDENTIFIER, "Name expected");

		GenericParameterParsing.parseList(builder);

		if(builder.getTokenType() == COLON)
		{
			parseWithSoftElements(new NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, Boolean>>()
			{
				@NotNull
				@Override
				public Pair<PsiBuilder.Marker, Boolean> fun(CSharpBuilderWrapper builderWrapper)
				{
					PsiBuilder.Marker mark = builderWrapper.mark();
					builderWrapper.advanceLexer();  // colon

					parseTypeList(builderWrapper, false);
					mark.done(EXTENDS_LIST);
					return new Pair<PsiBuilder.Marker, Boolean>(mark, Boolean.FALSE);
				}
			}, builder, GLOBAL_KEYWORD);
		}

		GenericParameterParsing.parseGenericConstraintList(builder);

		if(expect(builder, LBRACE, "'{' expected"))
		{
			while(!builder.eof() && builder.getTokenType() != RBRACE)
			{
				if(isEnum)
				{
					if(!parseEnumConstant(builder))
					{
						break;
					}
				}
				else
				{
					if(!DeclarationParsing.parse(builder, true))
					{
						break;
					}
				}
			}
			expect(builder, RBRACE, "'}' expected");
			expect(builder, SEMICOLON, null);
		}

		marker.done(TYPE_DECLARATION);
	}

	private static boolean parseEnumConstant(CSharpBuilderWrapper builder)
	{

		PsiBuilder.Marker mark = builder.mark();

		boolean nameExpected = false;
		if(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker modMark = builder.mark();
			parseAttributeList(builder);
			modMark.done(MODIFIER_LIST);

			nameExpected = true;
		}

		if(builder.getTokenType() == IDENTIFIER)
		{
			builder.advanceLexer();

			if(builder.getTokenType() == EQ)
			{
				builder.advanceLexer();

				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}
		}
		else
		{
			if(builder.getTokenType() == COMMA || builder.getTokenType() == RBRACE)
			{
				if(nameExpected)
				{
					builder.error("Name expected");
				}

				mark.done(ENUM_CONSTANT_DECLARATION);
				return false;
			}
		}

		mark.done(ENUM_CONSTANT_DECLARATION);

		if(builder.getTokenType() == COMMA)
		{
			builder.advanceLexer();
		}
		else
		{
			return false;
		}
		return true;
	}
}
