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
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.language.ast.IElementType;
import consulo.language.parser.PsiBuilder;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class EventParsing extends MemberWithBodyParsing
{
	public static void parse(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
	{
		if(parseType(builder, STUB_SUPPORT) == null)
		{
			builder.error("Type expected");
			done(marker, EVENT_DECLARATION);
		}
		else
		{
			TypeInfo implementType = DeclarationParsing.parseImplementType(builder);
			if(implementType == null)
			{
				DeclarationParsing.reportIdentifier(builder, STUB_SUPPORT);
				marker.done(EVENT_DECLARATION);
				return;
			}

			if(builder.getTokenType() == DOT)
			{
				builder.advanceLexer();
			}
			else
			{
				if(implementType.marker != null)
				{
					implementType.marker.rollbackTo();
				}
			}

			DeclarationParsing.doneThisOrIdentifier(builder);

			IElementType tokenType = builder.getTokenType();
			if(tokenType == COMMA)
			{
				done(marker, EVENT_DECLARATION);

				while(builder.getTokenType() == COMMA)
				{
					PsiBuilder.Marker marker2 = builder.mark();
					builder.advanceLexer();
					expectOrReportIdentifier(builder, STUB_SUPPORT);
					marker2.done(EVENT_DECLARATION);
				}
				expect(builder, SEMICOLON, "';' expected");
			}
			else if(tokenType == SEMICOLON)
			{
				builder.advanceLexer();
				done(marker, EVENT_DECLARATION);
			}
			else if(tokenType == EQ)
			{
				builder.advanceLexer();
				if(ExpressionParsing.parse(builder, set) == null)
				{
					builder.error("Expression expected");
				}
				expect(builder, SEMICOLON, "';' expected");
				done(marker, EVENT_DECLARATION);
			}
			else
			{
				parseAccessors(builder, XACCESSOR, EVENT_ACCESSOR_START);
				done(marker, EVENT_DECLARATION);
			}
		}
	}
}
