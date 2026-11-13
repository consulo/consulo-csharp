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
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.ast.IElementType;
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

	// what may follow the name of a record: record R(int X); record R<T> { } record R : B; record R where ...
	private static final TokenSet AFTER_RECORD_NAME = TokenSet.create(LPAR, LT, LBRACE, COLON, SEMICOLON);

	/**
	 * C# 9 {@code record R}, {@code record class R}, C# 10 {@code record struct R} - a soft keyword: a type named record
	 * is still a type before that
	 */
	public static boolean isRecordStart(CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() != CSharpTokens.IDENTIFIER || !builder.getVersion().isAtLeast(CSharpLanguageVersion._9_0) || !"record".equals(builder.getTokenText()))
		{
			return false;
		}
		IElementType next = builder.lookAhead(1);
		if(next == CLASS_KEYWORD || next == STRUCT_KEYWORD)
		{
			return true;
		}
		return next == CSharpTokens.IDENTIFIER && AFTER_RECORD_NAME.contains(builder.lookAhead(2));
	}

	public static void parse(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		boolean isEnum = builder.getTokenType() == ENUM_KEYWORD;
		boolean isInterface = builder.getTokenType() == INTERFACE_KEYWORD;

		if(isRecordStart(builder))
		{
			builder.remapCurrentToken(CSharpSoftTokens.RECORD_KEYWORD);
			builder.advanceLexer();

			if(builder.getTokenType() == CLASS_KEYWORD || builder.getTokenType() == STRUCT_KEYWORD)
			{
				builder.advanceLexer();
			}
		}
		else
		{
			builder.advanceLexer();
		}

		expectOrReportIdentifier(builder, STUB_SUPPORT);

		reportErrorUntil(builder, "Expected ':', '<', '(', '{' or 'where'", TokenSet.create(COLON, LT, LPAR, LBRACE, SEMICOLON), WHERE_SET);

		GenericParameterParsing.parseList(builder);

		// C# 9 positional record, C# 12 primary constructor - class C(int x)
		if(builder.getTokenType() == LPAR && !isEnum && !isInterface)
		{
			MethodParsing.parseParameterList(builder, STUB_SUPPORT, RPAR, ModifierSet.EMPTY);
		}

		reportErrorUntil(builder, "Expected ':', '{' or 'where'", TokenSet.create(COLON, LBRACE, SEMICOLON), WHERE_SET);

		if(builder.getTokenType() == COLON)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();  // colon
			if(parseExtendsTypeList(builder))
			{
				builder.error("Expected type");
			}
			mark.done(EXTENDS_LIST);
		}

		reportErrorUntil(builder, "Expected '{' or 'where'", TokenSet.create(LBRACE, SEMICOLON), WHERE_SET);

		GenericParameterParsing.parseGenericConstraintList(builder);

		// record R(int X); class C(int x);
		if(builder.getTokenType() == SEMICOLON)
		{
			builder.advanceLexer();
			done(marker, TYPE_DECLARATION);
			return;
		}

		reportErrorUntil(builder, "Expected '{'", TokenSet.create(LBRACE), TokenSet.EMPTY);

		if(expect(builder, LBRACE, "'{' expected"))
		{
			DeclarationParsing.parseAll(builder, false, isEnum);

			expect(builder, RBRACE, "'}' expected");
			expect(builder, SEMICOLON, null);
		}

		done(marker, TYPE_DECLARATION);
	}

	/**
	 * The base types - the first one may take the arguments of the primary constructor: {@code record D(int X) : B(X)}
	 *
	 * @return true if there is no type
	 */
	private static boolean parseExtendsTypeList(CSharpBuilderWrapper builder)
	{
		boolean empty = true;
		while(!builder.eof())
		{
			TypeInfo typeInfo = parseType(builder, STUB_SUPPORT);
			if(typeInfo == null)
			{
				if(!empty)
				{
					builder.error("Type expected");
				}
				break;
			}

			if(empty && builder.getTokenType() == LPAR)
			{
				ExpressionParsing.parseArgumentList(builder, false, ModifierSet.EMPTY, NONE);
			}

			empty = false;

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else
			{
				break;
			}
		}
		return empty;
	}
}
