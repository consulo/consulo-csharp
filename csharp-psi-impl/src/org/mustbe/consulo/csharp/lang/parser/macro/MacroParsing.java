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

package org.mustbe.consulo.csharp.lang.parser.macro;

import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.TokenSet;
import lombok.val;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class MacroParsing implements CSharpMacroTokens, CSharpMacroElements
{
	private static final TokenSet COND_STOPPERS = TokenSet.create(MACRO_ENDIF_KEYWORD, MACRO_ELSE_KEYWORD, MACRO_ELIF_KEYWORD);

	public static boolean parse(PsiBuilder builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		val token = builder.getTokenType();
		if(token == MACRO_DEFINE_KEYWORD || token == MACRO_UNDEF_KEYWORD)
		{
			builder.advanceLexer();

			if(builder.getTokenType() == MACRO_VALUE)
			{
				builder.advanceLexer();
			}
			else
			{
				builder.error("Identifier expected");
			}
			skipUntilStop(builder);
			mark.done(token == MACRO_UNDEF_KEYWORD ? MACRO_UNDEF : MACRO_DEFINE);
			return true;
		}
		else if(token == MACRO_IF_KEYWORD)
		{
			PsiBuilder.Marker condBlock = builder.mark();

			PsiBuilder.Marker startMarker = builder.mark();

			builder.advanceLexer();

			PsiBuilder.Marker parse = MacroExpressionParsing.parse(builder);
			if(parse == null)
			{
				builder.error("Expression expected");
			}

			SharingParsingHelpers.expect(builder, MACRO_STOP, null);
			startMarker.done(MACRO_BLOCK_START);

			parseAndDoneUntilCondStoppers(builder, condBlock);

			while(!builder.eof())
			{
				if(builder.getTokenType() == MACRO_ELIF_KEYWORD)
				{
					parseElIf(builder, startMarker);
				}
				else if(builder.getTokenType() == MACRO_ELSE_KEYWORD)
				{
					parseElse(builder, startMarker);
				}
				else if(builder.getTokenType() == MACRO_ENDIF_KEYWORD)
				{
					break;
				}
			}

			if(builder.getTokenType() == MACRO_ENDIF_KEYWORD)
			{
				PsiBuilder.Marker endIfMarker = builder.mark();
				builder.advanceLexer();

				SharingParsingHelpers.expect(builder, MACRO_STOP, null);

				endIfMarker.done(MACRO_BLOCK_STOP);
			}
			else
			{
				builder.error("'#endif' expected");
			}

			mark.done(MACRO_IF);

			return true;
		}
		else if(token == MACRO_REGION_KEYWORD)
		{
			PsiBuilder.Marker startMarker = builder.mark();
			builder.advanceLexer();
			skipUntilStop(builder);
			startMarker.done(MACRO_BLOCK_START);

			while(!builder.eof())
			{
				if(builder.getTokenType() == MACRO_ENDREGION_KEYWORD)
				{
					break;
				}
				builder.advanceLexer();
			}

			if(builder.getTokenType() == MACRO_ENDREGION_KEYWORD)
			{
				PsiBuilder.Marker endIfMarker = builder.mark();
				builder.advanceLexer();
				skipUntilStop(builder);
				endIfMarker.done(MACRO_BLOCK_STOP);
			}
			else
			{
				builder.error("'#endregion' expected");
			}

			mark.done(MACRO_BLOCK);

			return true;
		}
		else if(token == MACRO_ENDREGION_KEYWORD)
		{
			builder.advanceLexer();

			builder.error("'#endregion' without '#region'");

			skipUntilStop(builder);
			mark.done(MACRO_BLOCK_STOP);
			return true;
		}
		else if(token == MACRO_ENDIF_KEYWORD)
		{
			builder.advanceLexer();
			builder.error("'#endif' without '#if'");

			skipUntilStop(builder);
			mark.done(MACRO_BLOCK_STOP);
			return true;
		}
		else
		{
			builder.advanceLexer();

			mark.drop();
			return false;
		}
	}

	private static void parseElse(PsiBuilder builder, PsiBuilder.Marker parentMarker)
	{
		PsiBuilder.Marker mark = builder.mark();

		PsiBuilder.Marker headerMarker = builder.mark();

		if(parentMarker == null)
		{
			builder.error("#if block not opened");
		}

		builder.advanceLexer();
		SharingParsingHelpers.expect(builder, MACRO_STOP, null);

		headerMarker.done(MACRO_BLOCK_START);

		parseAndDoneUntilCondStoppers(builder, mark);
	}

	private static void parseAndDoneUntilCondStoppers(PsiBuilder builder, PsiBuilder.Marker marker)
	{
		while(!builder.eof())
		{
			if(COND_STOPPERS.contains(builder.getTokenType()))
			{
				break;
			}

			MacroParsing.parse(builder);
		}

		marker.done(MACRO_IF_CONDITION_BLOCK);
	}

	private static void parseElIf(PsiBuilder builder, PsiBuilder.Marker parentMarker)
	{
		PsiBuilder.Marker mark = builder.mark();

		PsiBuilder.Marker headerMarker = builder.mark();

		if(parentMarker == null)
		{
			builder.error("#if block not opened");
		}

		builder.advanceLexer();

		PsiBuilder.Marker parse = MacroExpressionParsing.parse(builder);
		if(parse == null)
		{
			builder.error("Expression expected");
		}

		SharingParsingHelpers.expect(builder, MACRO_STOP, null);

		headerMarker.done(MACRO_BLOCK_START);

		while(!builder.eof())
		{
			if(COND_STOPPERS.contains(builder.getTokenType()))
			{
				break;
			}

			MacroParsing.parse(builder);
		}

		mark.done(MACRO_IF_CONDITION_BLOCK);
	}

	private static void skipUntilStop(PsiBuilder builder)
	{
		while(!builder.eof())
		{
			if(builder.getTokenType() == MACRO_STOP)
			{
				builder.advanceLexer();
				break;
			}
			else
			{
				builder.advanceLexer();
			}
		}
	}
}
