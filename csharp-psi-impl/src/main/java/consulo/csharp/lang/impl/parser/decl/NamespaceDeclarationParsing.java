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
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.language.parser.PsiBuilder;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class NamespaceDeclarationParsing extends SharedParsingHelpers
{
	public static void parse(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		builder.advanceLexer();

		if(ExpressionParsing.parseQualifiedReference(builder, null) == null)
		{
			builder.error("Name expected");
		}

		if(builder.getTokenType() == SEMICOLON)
		{
			builder.advanceLexer();

			marker.done(NAMESPACE_STATEMENT);
		}
		else
		{
			if(expect(builder, LBRACE, "'{' expected"))
			{
				DeclarationParsing.parseAll(builder, false, false);

				expect(builder, RBRACE, "'}' expected");
				expect(builder, SEMICOLON, null);
			}

			marker.done(NAMESPACE_DECLARATION);
		}
	}

}
