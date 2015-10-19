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

import java.util.Deque;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPreprocessorParsing
{
	//private static final TokenSet COND_STOPPERS = TokenSet.create(ENDIF_KEYWORD, ELSE_KEYWORD, ELIF_KEYWORD);

	public static boolean parseDirectives(PsiBuilder builder, Deque<PsiBuilder.Marker> regionMarkers, boolean wantBreak)
	{
		while(!builder.eof())
		{
			boolean needBreak = wantParseSharp(builder, regionMarkers);
			if(wantBreak && needBreak)
			{
				break;
			}
		}
		return false;
	}

	public static boolean wantParseSharp(PsiBuilder builder, Deque<PsiBuilder.Marker> regionMarkers)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == CSharpMacroTokens.SHARP)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			return parseDirective(builder, mark, regionMarkers);
		}
		else
		{
			builder.advanceLexer();
		}
		return false;
	}

	public static boolean parseDirective(@NotNull PsiBuilder builder, @NotNull PsiBuilder.Marker mark, @NotNull Deque<PsiBuilder.Marker> regionMarkers)
	{
		IElementType tokenType = builder.getTokenType();
		/*if(token == DEFINE_KEYWORD || token == UNDEF_KEYWORD)
		{
			builder.advanceLexer();

			if(builder.getTokenType() == SIMPLE_VALUE)
			{
				builder.advanceLexer();
			}
			else
			{
				builder.error("Identifier expected");
			}
			skipUntilStop(builder);
			mark.done(token == UNDEF_KEYWORD ? MACRO_UNDEF : MACRO_DEFINE);
			return true;
		}
		else if(token == IF_KEYWORD)
		{
			PsiBuilder.Marker condBlock = builder.mark();

			PsiBuilder.Marker startMarker = builder.mark();

			builder.advanceLexer();

			PsiBuilder.Marker parse = MacroExpressionParsing.parse(builder);
			if(parse == null)
			{
				builder.error("Expression expected");
			}

			SharedParsingHelpers.expect(builder, MACRO_STOP, null);
			startMarker.done(MACRO_BLOCK_START);

			parseAndDoneUntilCondStoppers(builder, condBlock);

			while(!builder.eof())
			{
				if(builder.getTokenType() == ELIF_KEYWORD)
				{
					parseElIf(builder, startMarker);
				}
				else if(builder.getTokenType() == ELSE_KEYWORD)
				{
					parseElse(builder, startMarker);
				}
				else if(builder.getTokenType() == ENDIF_KEYWORD)
				{
					break;
				}
			}

			if(builder.getTokenType() == ENDIF_KEYWORD)
			{
				PsiBuilder.Marker endIfMarker = builder.mark();
				builder.advanceLexer();

				SharedParsingHelpers.expect(builder, MACRO_STOP, null);

				endIfMarker.done(MACRO_BLOCK_STOP);
			}
			else
			{
				builder.error("'#endif' expected");
			}

			mark.done(MACRO_IF);

			return true;
		}
		else */
		if(tokenType == CSharpMacroTokens.REGION_KEYWORD)
		{
			builder.advanceLexer();

			advanceUntilFragment(builder);

			mark.done(CSharpMacroElements.MACRO_BLOCK_START);

			regionMarkers.add(mark.precede());

			parseDirectives(builder, regionMarkers, true);

			return true;
		}
		else if(tokenType == CSharpMacroTokens.ENDREGION_KEYWORD)
		{
			builder.advanceLexer();

			PsiBuilder.Marker mainMarker = regionMarkers.pollLast();
			if(mainMarker != null)
			{
				advanceUntilFragment(builder);

				mark.done(CSharpMacroElements.MACRO_BLOCK_START);

				mainMarker.done(CSharpMacroElements.MACRO_BLOCK);
			}
			else
			{
				advanceUntilFragment(builder);

				mark.done(CSharpMacroElements.MACRO_BLOCK_START);

				mark.precede().done(CSharpMacroElements.MACRO_BLOCK);
			}

			return regionMarkers.isEmpty();
		}  /*
		else if(token == ENDIF_KEYWORD)
		{
			builder.advanceLexer();
			builder.error("'#endif' without '#if'");

			skipUntilStop(builder);
			mark.done(MACRO_BLOCK_STOP);
			return true;
		} */
		else
		{
			builder.advanceLexer();

			mark.drop();
			return false;
		}
	}

/*	private static void parseElse(PsiBuilder builder, PsiBuilder.Marker parentMarker)
	{
		PsiBuilder.Marker mark = builder.mark();

		PsiBuilder.Marker headerMarker = builder.mark();

		if(parentMarker == null)
		{
			builder.error("#if block not opened");
		}

		builder.advanceLexer();
		SharedParsingHelpers.expect(builder, MACRO_STOP, null);

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

			CSharpPreprocessorParsing.parse(builder);
		}

		marker.done(MACRO_IF_CONDITION_BLOCK);
	} */
 /*
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

		SharedParsingHelpers.expect(builder, MACRO_STOP, null);

		headerMarker.done(MACRO_BLOCK_START);

		while(!builder.eof())
		{
			if(COND_STOPPERS.contains(builder.getTokenType()))
			{
				break;
			}

			CSharpPreprocessorParsing.parse(builder);
		}

		mark.done(MACRO_IF_CONDITION_BLOCK);
	}       */

	private static void advanceUntilFragment(PsiBuilder builder)
	{
		while(!builder.eof())
		{
			IElementType tokenType = builder.getTokenType();
			if(tokenType == CSharpMacroTokens.CSHARP_FRAGMENT || tokenType == CSharpMacroTokens.SHARP)
			{
				break;
			}
			else
			{
				builder.advanceLexer();
			}
		}
	}
}
