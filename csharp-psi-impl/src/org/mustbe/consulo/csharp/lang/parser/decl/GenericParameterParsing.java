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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullFunction;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class GenericParameterParsing extends SharedParsingHelpers
{
	private static final TokenSet ourGenericStoppers = TokenSet.create(GT, LBRACE, LPAR, RBRACE);

	public static void parseList(CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() != LT)
		{
			return;
		}

		PsiBuilder.Marker mark = builder.mark();
		builder.advanceLexer();

		while(!builder.eof())
		{
			if(ourGenericStoppers.contains(builder.getTokenType()))
			{
				break;
			}

			parse(builder);

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else if(!ourGenericStoppers.contains(builder.getTokenType()))
			{
				PsiBuilder.Marker errorMarker = builder.mark();
				builder.advanceLexer();
				errorMarker.error("Expected identifier");
			}
		}

		expect(builder, GT, "'>' expected");

		mark.done(GENERIC_PARAMETER_LIST);
	}

	public static void parse(CSharpBuilderWrapper builder)
	{
		val marker = builder.mark();

		parseModifierListWithAttributes(builder, STUB_SUPPORT);

		expectOrReportIdentifier(builder, STUB_SUPPORT);

		marker.done(GENERIC_PARAMETER);
	}

	public static PsiBuilder.Marker parseGenericConstraintList(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker marker = builder.mark();

		boolean empty = true;
		while(true)
		{
			Pair<PsiBuilder.Marker, Void> p = parseWithSoftElements(new NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, Void>>()
			{
				@NotNull
				@Override
				public Pair<PsiBuilder.Marker, Void> fun(CSharpBuilderWrapper builderWrapper)
				{
					return new Pair<PsiBuilder.Marker, Void>(parseGenericConstraint(builderWrapper), null);
				}
			}, builder, WHERE_KEYWORD);

			if(p.getFirst() == null)
			{
				break;
			}
			else
			{
				empty = false;
			}
		}

		if(empty)
		{
			marker.drop();
		}
		else
		{
			marker.done(GENERIC_CONSTRAINT_LIST);
		}
		return marker;
	}

	private static PsiBuilder.Marker parseGenericConstraint(CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() != WHERE_KEYWORD)
		{
			return null;
		}

		PsiBuilder.Marker marker = builder.mark();

		builder.advanceLexer();

		doneOneElement(builder, CSharpTokens.IDENTIFIER, REFERENCE_EXPRESSION, "Identifier expected");
		if(expect(builder, COLON, "Colon expected"))
		{
			while(!builder.eof())
			{
				PsiBuilder.Marker value = builder.mark();
				IElementType doneElement = null;

				if(builder.getTokenType() == CLASS_KEYWORD || builder.getTokenType() == STRUCT_KEYWORD || builder.getTokenType() == NEW_KEYWORD)
				{
					boolean newKeyword = builder.getTokenType() == NEW_KEYWORD;
					builder.advanceLexer();
					if(newKeyword)
					{
						expect(builder, LPAR, "'(' expected");
						expect(builder, RPAR, "')' expected");
					}

					doneElement = GENERIC_CONSTRAINT_KEYWORD_VALUE;
				}
				else
				{
					if(parseType(builder, STUB_SUPPORT) == null)
					{
						builder.error("Type expected");
					}
					doneElement = GENERIC_CONSTRAINT_TYPE_VALUE;
				}
				value.done(doneElement);

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else
				{
					break;
				}
			}
		}

		marker.done(GENERIC_CONSTRAINT);
		return marker;
	}
}
