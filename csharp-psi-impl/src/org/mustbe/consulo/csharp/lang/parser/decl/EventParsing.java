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
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 04.12.13.
 */
public class EventParsing extends MemberWithBodyParsing
{
	public static void parse(CSharpBuilderWrapper builder, PsiBuilder.Marker marker)
	{
		if(parseType(builder, NONE) == null)
		{
			builder.error("Type expected");
		}
		else
		{
			expect(builder, IDENTIFIER, "Identifier expected");
			IElementType tokenType = builder.getTokenType();
			if(tokenType == SEMICOLON)
			{
				builder.advanceLexer();
			}
			else if(tokenType == EQ)
			{
				builder.advanceLexer();
				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
				expect(builder, SEMICOLON, "';' expected");
			}
			else
			{
				parseAccessors(builder, XXX_ACCESSOR, EVENT_ACCESSOR_START);
			}
		}

		marker.done(EVENT_DECLARATION);
	}

}
