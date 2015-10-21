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

package org.mustbe.consulo.csharp.lang.parser;

import java.util.Deque;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesBinders;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPreprocessorParsing
{
	public static boolean parseDirectives(PsiBuilder builder, @NotNull Deque<PsiBuilder.Marker> regionMarkers, @NotNull Deque<PsiBuilder.Marker> ifMarkers, boolean wantBreak)
	{
		while(!builder.eof())
		{
			boolean needBreak = wantParseSharp(builder, regionMarkers, ifMarkers);
			if(wantBreak && needBreak)
			{
				break;
			}
		}
		return false;
	}

	private static boolean wantParseSharp(PsiBuilder builder, @NotNull Deque<PsiBuilder.Marker> regionMarkers, @NotNull Deque<PsiBuilder.Marker> ifMarkers)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == CSharpPreprocessorTokens.SHARP)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			return parseDirective(builder, mark, regionMarkers, ifMarkers);
		}
		else
		{
			builder.advanceLexer();
		}
		return false;
	}

	public static boolean parseDirective(@NotNull PsiBuilder builder, @NotNull PsiBuilder.Marker mark, @NotNull Deque<PsiBuilder.Marker> regionMarkers, Deque<PsiBuilder.Marker> ifMarkers)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == CSharpPreprocessorTokens.DEFINE_KEYWORD || tokenType == CSharpPreprocessorTokens.UNDEF_KEYWORD)
		{
			builder.advanceLexer();

			advanceUntilFragment(builder);

			doneWithBinder(mark, tokenType == CSharpPreprocessorTokens.UNDEF_KEYWORD ? CSharpPreprocessorElements.UNDEF_DIRECTIVE : CSharpPreprocessorElements.DEFINE_DIRECTIVE);
			return true;
		}
		else if(tokenType == CSharpPreprocessorTokens.IF_KEYWORD ||
				tokenType == CSharpPreprocessorTokens.ELIF_KEYWORD ||
				tokenType == CSharpPreprocessorTokens.ELSE_KEYWORD ||
				tokenType == CSharpPreprocessorTokens.ENDIF_KEYWORD)
		{
			builder.advanceLexer();

			if(tokenType == CSharpPreprocessorTokens.IF_KEYWORD || tokenType == CSharpPreprocessorTokens.ELIF_KEYWORD)
			{
				PsiBuilder.Marker expressionMarker = CSharpPreprocessorExpressionParsing.parse(builder);
				if(expressionMarker == null)
				{
					builder.error("Expression expected");
				}
			}

			advanceUntilFragment(builder);

			mark.done(CSharpPreprocessorElements.OPEN_TAG);

			return true;
		}
		else if(tokenType == CSharpPreprocessorTokens.REGION_KEYWORD)
		{
			builder.advanceLexer();

			advanceUntilFragment(builder);

			mark.done(CSharpPreprocessorElements.OPEN_TAG);

			regionMarkers.add(mark.precede());

			parseDirectives(builder, regionMarkers, ifMarkers, true);

			return true;
		}
		else if(tokenType == CSharpPreprocessorTokens.ENDREGION_KEYWORD)
		{
			return doneEndTag(builder, mark, CSharpPreprocessorElements.REGION_BLOCK, regionMarkers);
		}
		else if(tokenType == CSharpPreprocessorTokens.ENDIF_KEYWORD)
		{
			return doneEndTag(builder, mark, CSharpPreprocessorElements.CONDITION_BLOCK, ifMarkers);
		}
		else
		{
			advanceUntilFragment(builder);

			mark.done(CSharpPreprocessorElements.OPEN_TAG);
			return false;
		}
	}

	private static boolean doneEndTag(PsiBuilder builder, PsiBuilder.Marker mark, IElementType doneTo, Deque<PsiBuilder.Marker> deque)
	{
		builder.advanceLexer();

		PsiBuilder.Marker mainMarker = deque.pollLast();
		if(mainMarker != null)
		{
			advanceUntilFragment(builder);

			doneWithBinder(mark, CSharpPreprocessorElements.CLOSE_TAG);

			mainMarker.done(doneTo);
		}
		else
		{
			advanceUntilFragment(builder);

			doneWithBinder(mark, CSharpPreprocessorElements.CLOSE_TAG);

			mark.precede().done(doneTo);
		}

		return deque.isEmpty();
	}

	private static void doneWithBinder(PsiBuilder.Marker mark, IElementType elementType)
	{
		mark.done(elementType);
		mark.setCustomEdgeTokenBinders(null, WhitespacesBinders.GREEDY_RIGHT_BINDER);
	}

	private static void advanceUntilFragment(PsiBuilder builder)
	{
		while(!builder.eof())
		{
			IElementType tokenType = builder.getTokenType();
			if(tokenType == CSharpPreprocessorTokens.CSHARP_FRAGMENT || tokenType == CSharpPreprocessorTokens.SHARP)
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
