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
import org.mustbe.consulo.csharp.lang.parser.UsingStatementParsing;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import com.intellij.lang.PsiBuilder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullFunction;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class DeclarationParsing extends SharedParsingHelpers
{
		// { (
	private static final TokenSet NAME_STOPPERS = TokenSet.create(LBRACE, LPAR, THIS_KEYWORD);

	private static final TokenSet NAME_TOKENS = TokenSet.create(THIS_KEYWORD, IDENTIFIER);

	public static void parseAll(@NotNull CSharpBuilderWrapper builder, boolean root, boolean isEnum)
	{
		if(isEnum)
		{
			IElementType prevToken;
			while(!builder.eof())
			{
				prevToken = builder.getTokenType();

				parseEnumConstant(builder);

				if(builder.getTokenType() == COMMA)
				{
					if(prevToken == COMMA)
					{
						builder.error("Name expected");
					}
					builder.advanceLexer();
				}
				else if(builder.getTokenType() == RBRACE)
				{
					break;
				}
				else
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					errorMarker.error("Expected comma");
				}
			}
		}
		else
		{
			while(!builder.eof())
			{
				if(!root && builder.getTokenType() == RBRACE)
				{
					return;
				}

				if(!parse(builder, root))
				{
					PsiBuilder.Marker mark = builder.mark();
					builder.advanceLexer();
					mark.error("Unexpected token");
				}
			}
		}
	}

	private static boolean parse(@NotNull CSharpBuilderWrapper builder, boolean root)
	{
		PsiBuilder.Marker marker = builder.mark();

		Pair<PsiBuilder.Marker, Boolean> modifierListPair = parseWithSoftElements(new NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker,
				Boolean>>()
		{
			@NotNull
			@Override
			public Pair<PsiBuilder.Marker, Boolean> fun(CSharpBuilderWrapper builderWrapper)
			{
				return parseModifierListWithAttributes(builderWrapper, STUB_SUPPORT);
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

			FieldOrPropertyParsing.parseFieldOrLocalVariableAtTypeWithDone(builder, marker, FIELD_DECLARATION, STUB_SUPPORT, true);
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

				MethodParsing.parseMethodStartAfterType(builder, marker, null, MethodParsing.Target.DECONSTRUCTOR);
			}
			else
			{
				TypeInfo typeInfo = parseType(builder, STUB_SUPPORT);
				if(typeInfo == null)
				{
					if(!modifierListPair.getSecond())
					{
						if(root)
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
				else
				{
					TypeInfo implementType = parseImplementType(builder);
					if(implementType == null)
					{
						builder.error("Name is expected");

						// if we dont have name but we have lbracket - parse as index method
						parseAfterName(builder, marker, builder.getTokenType() == LBRACKET ? THIS_KEYWORD : null);
						return true;
					}

					IElementType prevToken = null;
					if(builder.getTokenType() == DOT)
					{
						builder.advanceLexer();

						prevToken = builder.getTokenType();

						expect(builder, NAME_TOKENS, "Name is expected");
					}
					else
					{
						if(implementType.marker != null)
						{
							implementType.marker.rollbackTo();
						}

						prevToken = builder.getTokenType();

						expect(builder, NAME_TOKENS, "Name is expected");
					}

					parseAfterName(builder, marker, prevToken);
				}
			}
		}
		return true;
	}

	private static boolean parseEnumConstant(CSharpBuilderWrapper builder)
	{
		if(builder.getTokenType() == RBRACE)
		{
			return true;
		}
		PsiBuilder.Marker mark = builder.mark();

		boolean nameExpected = false;
		if(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker modMark = builder.mark();
			parseAttributeList(builder, STUB_SUPPORT);
			modMark.done(CSharpStubElements.MODIFIER_LIST);

			nameExpected = true;
		}

		if(builder.getTokenType() == IDENTIFIER)
		{
			if(!nameExpected)
			{
				emptyElement(builder, CSharpStubElements.MODIFIER_LIST);
			}

			builder.advanceLexer();

			if(builder.getTokenType() == EQ)
			{
				builder.advanceLexer();

				if(ExpressionParsing.parse(builder) == null)
				{
					builder.error("Expression expected");
				}
			}
		}
		else
		{
			if(builder.getTokenType() == COMMA || builder.getTokenType() == RBRACE)
			{
				if(nameExpected)
				{
					builder.error("Name expected");
				}

				done(mark, ENUM_CONSTANT_DECLARATION);
				return false;
			}
		}

		done(mark, ENUM_CONSTANT_DECLARATION);
		return true;
	}

	private static void parseAfterName(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType prevToken)
	{
		if(prevToken == THIS_KEYWORD)
		{
			FieldOrPropertyParsing.parseArrayAfterThis(builder, marker);
		}
		else if(builder.getTokenType() == LPAR || builder.getTokenType() == LT) // MODIFIER_LIST TYPE IDENTIFIER LPAR -> METHOD
		{
			MethodParsing.parseMethodStartAfterName(builder, marker, MethodParsing.Target.METHOD);
		}
		else
		{
			FieldOrPropertyParsing.parseFieldOrPropertyAfterName(builder, marker);
		}
	}

	private static TypeInfo parseImplementType(CSharpBuilderWrapper builder)
	{
		IElementType tokenType = builder.getTokenType();
		if(tokenType == THIS_KEYWORD)
		{
			return new TypeInfo();
		}
		return parseType(builder, STUB_SUPPORT, NAME_STOPPERS);
	}
}
