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

package consulo.csharp.lang.impl.parser.decl;

import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;

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

		expectOrReportIdentifier(builder, STUB_SUPPORT);

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
			DeclarationParsing.parseAll(builder, false, isEnum);

			expect(builder, RBRACE, "'}' expected");
			expect(builder, SEMICOLON, null);
		}

		done(marker, TYPE_DECLARATION);
	}
}
