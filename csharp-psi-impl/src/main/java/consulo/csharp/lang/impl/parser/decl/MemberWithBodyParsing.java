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

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.PsiBuilder;
import consulo.util.lang.Pair;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class MemberWithBodyParsing extends SharedParsingHelpers
{
	protected static void parseAccessors(CSharpBuilderWrapper builder, IElementType to, TokenSet tokenSet)
	{
		if(expect(builder, LBRACE, "'{' expected"))
		{
			while(!builder.eof())
			{
				if(builder.getTokenType() == RBRACE)
				{
					break;
				}

				parseAccessor(builder, to, tokenSet);
			}

			expect(builder, RBRACE, "'}' expected");
		}
	}

	private static void parseAccessor(CSharpBuilderWrapper builder, IElementType to, TokenSet tokenSet)
	{
		PsiBuilder.Marker marker = builder.mark();

		Pair<PsiBuilder.Marker, ModifierSet> pairModifierList = parseModifierListWithAttributes(builder, STUB_SUPPORT);

		builder.enableSoftKeywords(tokenSet);
		boolean contains = tokenSet.contains(builder.getTokenType());
		builder.disableSoftKeywords(tokenSet);

		if(contains)
		{
			builder.advanceLexer();

			MethodParsing.parseMethodBodyOrSemicolon(builder, pairModifierList.getSecond());

			marker.done(to);
		}
		else
		{
			// non empty
			if(!pairModifierList.getSecond().isEmpty())
			{
				marker.drop();
				builder.error("Expected accessor name");
			}
			else
			{
				marker.rollbackTo();
				PsiBuilder.Marker errorMarker = builder.mark();
				builder.advanceLexer(); // advance one element
				errorMarker.error("Expected accessor name");
			}
		}
	}
}
