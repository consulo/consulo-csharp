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

package consulo.csharp.lang.impl.parser;

import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;

import jakarta.annotation.Nonnull;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class UsingStatementParsing extends SharedParsingHelpers
{
	public static final TokenSet ourStoppers = TokenSet.orSet(ourSemicolonSet, CSharpTokenSets.MODIFIERS, CSharpTokenSets.TYPE_DECLARATION_START, TokenSet.create(DELEGATE_KEYWORD, CONST_KEYWORD));

	public static void parseUsing(@Nonnull CSharpBuilderWrapper builder, @Nonnull PsiBuilder.Marker marker, @Nonnull Consumer<CSharpBuilderWrapper> consumer)
	{
		consumer.accept(builder);

		IElementType to = null;
		if(builder.getTokenType() == CSharpTokens.IDENTIFIER && builder.lookAhead(1) == CSharpTokens.EQ)
		{
			doneIdentifier(builder, STUB_SUPPORT);

			builder.advanceLexer();

			if(parseType(builder, STUB_SUPPORT) == null)
			{
				builder.error("Type expected");
			}
			to = TYPE_DEF_STATEMENT;
		}
		else if(builder.getTokenType() == CSharpTokens.STATIC_KEYWORD)
		{
			builder.advanceLexer();

			if(parseType(builder, STUB_SUPPORT) == null)
			{
				builder.error("Type expected");
			}
			to = USING_TYPE_STATEMENT;
		}
		else
		{
			to = USING_NAMESPACE_STATEMENT;

			ExpressionParsing.parseQualifiedReference(builder, null);
		}

		reportErrorUntil(builder, "';' expected", ourStoppers, TokenSet.EMPTY);

		expect(builder, CSharpTokens.SEMICOLON, "';' expected");

		marker.done(to);
	}
}
