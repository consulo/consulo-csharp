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

import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class TypeDeclarationParsing extends SharedParsingHelpers
{
	private static final TokenSet WHERE_SET = TokenSet.create(CSharpSoftTokens.WHERE_KEYWORD);

	public static void parse(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		boolean isEnum = builder.getTokenType() == ENUM_KEYWORD;

		builder.advanceLexer();

		expect(builder, IDENTIFIER, "Name expected");

		reportErrorUntil(builder, "Expected ':', '<', '{' or 'where'", TokenSet.create(COLON, LT, LBRACE), WHERE_SET);

		GenericParameterParsing.parseList(builder);

		reportErrorUntil(builder, "Expected ':', '{' or 'where'", TokenSet.create(COLON, LBRACE), WHERE_SET);

		if(builder.getTokenType() == COLON)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();  // colon
			if(parseTypeList(builder, STUB_SUPPORT))
			{
				builder.error("Expected type");
			}
			mark.done(EXTENDS_LIST);
		}

		reportErrorUntil(builder, "Expected '{' or 'where'", TokenSet.create(LBRACE), WHERE_SET);

		GenericParameterParsing.parseGenericConstraintList(builder);

		reportErrorUntil(builder, "Expected '{'", TokenSet.create(LBRACE), TokenSet.EMPTY);

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

		done(marker, TYPE_DECLARATION);
	}

	private static boolean parseEnumConstant(CSharpBuilderWrapper builder)
	{

		PsiBuilder.Marker mark = builder.mark();

		boolean nameExpected = false;
		if(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker modMark = builder.mark();
			parseAttributeList(builder, STUB_SUPPORT);
			modMark.done(CSharpStubElements.MODIFIER_LIST);

			nameExpected = true;
		}

		if(builder.getTokenType() == IDENTIFIER)
		{
			if(!nameExpected)
			{
				emptyElement(builder, CSharpStubElements.MODIFIER_LIST);
			}

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

				done(mark, ENUM_CONSTANT_DECLARATION);
				return false;
			}
		}

		done(mark, ENUM_CONSTANT_DECLARATION);

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
