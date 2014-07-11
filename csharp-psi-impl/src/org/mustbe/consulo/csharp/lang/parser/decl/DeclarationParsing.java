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
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.UsingStatementParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullFunction;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class DeclarationParsing extends SharingParsingHelpers
{
	// { (
	private static final TokenSet NAME_STOPPERS = TokenSet.create(LBRACE, LPAR);

	public static boolean parse(@NotNull CSharpBuilderWrapper builder, boolean inner)
	{
		if(inner && builder.getTokenType() == RBRACE)
		{
			return false;
		}

		PsiBuilder.Marker marker = builder.mark();

		Pair<PsiBuilder.Marker, Boolean> modifierListPair = parseWithSoftElements(new NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker,
				Boolean>>()
		{
			@NotNull
			@Override
			public Pair<PsiBuilder.Marker, Boolean> fun(CSharpBuilderWrapper builderWrapper)
			{
				return parseModifierListWithAttributes(builderWrapper);
			}
		}, builder, PARTIAL_KEYWORD, ASYNC_KEYWORD);

		PsiBuilder.Marker modifierListMarker = modifierListPair.getFirst();

		val tokenType = builder.getTokenType();
		if(tokenType == NAMESPACE_KEYWORD)
		{
			NamespaceDeclarationParsing.parse(builder, marker);
		}
		else if(CSharpTokenSets.TYPE_DECLARATION_START.contains(tokenType))
		{
			TypeDeclarationParsing.parse(builder, marker);
		}
		else if(tokenType == EVENT_KEYWORD)
		{
			builder.advanceLexer();

			EventParsing.parse(builder, marker);
		}
		else if(tokenType == DELEGATE_KEYWORD)
		{
			builder.advanceLexer();

			MethodParsing.parseMethodStartAtType(builder, marker);
		}
		else if(tokenType == USING_KEYWORD)
		{
			UsingStatementParsing.parseUsingList(builder, marker);
		}
		else if(tokenType == CONST_KEYWORD)
		{
			builder.advanceLexer();

			FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(builder, marker, FIELD_DECLARATION);

			expect(builder, SEMICOLON, "';' expected");
		}
		else
		{
			// MODIFIER_LIST IDENTIFIER LPAR -> CONSTRUCTOR
			if(tokenType == IDENTIFIER && builder.lookAhead(1) == LPAR)
			{
				MethodParsing.parseMethodStartAfterType(builder, marker, null, MethodParsing.Target.CONSTRUCTOR);
			}
			else if(tokenType == TILDE)
			{
				builder.advanceLexer();

				MethodParsing.parseMethodStartAfterType(builder, marker, null, MethodParsing.Target.CONSTRUCTOR);
			}
			else
			{
				TypeInfo typeInfo = parseType(builder, BracketFailPolicy.NOTHING, false);
				if(typeInfo == null)
				{
					if(!modifierListPair.getSecond())
					{
						if(!inner)
						{
							marker.done(DUMMY_DECLARATION);
							return true;
						}
						builder.error("Type expected");
					}
					else
					{
						modifierListMarker.drop();
					}

					marker.drop();
					return false;
				}
				else if(builder.getTokenType() == OPERATOR_KEYWORD)
				{
					MethodParsing.parseMethodStartAfterType(builder, marker, typeInfo, MethodParsing.Target.METHOD);
				}
				else if(builder.getTokenType() == THIS_KEYWORD)
				{
					builder.advanceLexer();

					FieldOrPropertyParsing.parseArrayAfterThis(builder, marker);
				}
				else
				{
					TypeInfo implementType = parseType(builder, BracketFailPolicy.NOTHING, false, NAME_STOPPERS);
					if(implementType == null)
					{
						modifierListMarker.drop();
						marker.drop();
						return false;
					}

					if(builder.getTokenType() == DOT)
					{
						builder.advanceLexer();

						expect(builder, IDENTIFIER, "Name is expected");
					}
					else
					{
						implementType.marker.rollbackTo();

						expect(builder, IDENTIFIER, "Name is expected");
					}

					// MODIFIER_LIST TYPE IDENTIFIER LPAR -> METHOD
					if(builder.getTokenType() == LPAR || builder.getTokenType() == LT)
					{
						MethodParsing.parseMethodStartAfterName(builder, marker, MethodParsing.Target.METHOD);
					}
					else
					{
						FieldOrPropertyParsing.parseFieldOrPropertyAfterName(builder, marker);
					}
				}
			}
		}
		return true;
	}
}
