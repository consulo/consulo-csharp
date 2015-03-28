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
 * @since 28.11.13.
 */
public class FieldOrPropertyParsing extends MemberWithBodyParsing
{
	public static void parseFieldOrLocalVariableAtTypeWithDone(CSharpBuilderWrapper builder,
			PsiBuilder.Marker marker,
			IElementType to,
			int typeFlags,
			boolean semicolonEat)
	{
		TypeInfo typeInfo = parseType(builder, typeFlags);
		if(typeInfo == null)
		{
			builder.error("Type expected");

			if(semicolonEat)
			{
				expect(builder, SEMICOLON, "';' expected");
			}
			marker.done(to);
		}
		else
		{
			parseFieldOrLocalVariableAtNameWithDone(builder, marker, to, typeFlags, semicolonEat);
		}
	}

	public static boolean parseFieldOrLocalVariableAtNameWithDone(CSharpBuilderWrapper builder,
			PsiBuilder.Marker marker,
			IElementType to,
			int typeFlags,
			boolean semicolonEat)
	{
		if(builder.getTokenType() == IDENTIFIER)
		{
			builder.advanceLexer();

			parseFieldAfterName(builder, marker, to, typeFlags, semicolonEat);
			return true;
		}
		else
		{
			builder.error("Name expected");

			if(semicolonEat)
			{
				expect(builder, SEMICOLON, "';' expected");
			}

			marker.done(to);
			return false;
		}
	}

	private static PsiBuilder.Marker parseFieldAfterName(CSharpBuilderWrapper builder,
			PsiBuilder.Marker marker,
			IElementType to,
			int typeFlags,
			boolean semicolonEat)
	{
		if(builder.getTokenType() == EQ || builder.getTokenType() == DARROW)
		{
			builder.advanceLexer();
			if(ExpressionParsing.parseVariableInitializer(builder) == null)
			{
				builder.error("Expression expected");
			}
		}

		if(builder.getTokenType() == COMMA)
		{
			builder.advanceLexer();

			marker.done(to);

			PsiBuilder.Marker newMarker = builder.mark();

			parseFieldOrLocalVariableAtNameWithDone(builder, newMarker, to, typeFlags, semicolonEat);

			return marker;
		}
		else
		{
			if(semicolonEat)
			{
				expect(builder, SEMICOLON, "';' expected");
			}

			done(marker, to);

			return marker;
		}
	}

	public static void parseArrayAfterThis(CSharpBuilderWrapper builderWrapper, PsiBuilder.Marker marker)
	{
		if(builderWrapper.getTokenType() == LBRACKET)
		{
			MethodParsing.parseParameterList(builderWrapper, STUB_SUPPORT, RBRACKET);
		}
		else
		{
			builderWrapper.error("'[' expected");
		}

		parseAccessors(builderWrapper, XXX_ACCESSOR, PROPERTY_ACCESSOR_START);

		done(marker, ARRAY_METHOD_DECLARATION);
	}

	public static void parseFieldOrPropertyAfterName(CSharpBuilderWrapper builderWrapper, PsiBuilder.Marker marker)
	{
		if(builderWrapper.getTokenType() == LBRACE)
		{
			parseAccessors(builderWrapper, XXX_ACCESSOR, PROPERTY_ACCESSOR_START);

			if(builderWrapper.getTokenType() == EQ)
			{
				builderWrapper.advanceLexer();
				if(ExpressionParsing.parseVariableInitializer(builderWrapper) == null)
				{
					builderWrapper.error("Expression expected");
				}
				expect(builderWrapper, SEMICOLON, "';' expected");
			}

			marker.done(PROPERTY_DECLARATION);
		}
		else
		{
			parseFieldAfterName(builderWrapper, marker, FIELD_DECLARATION, STUB_SUPPORT, true);
		}
	}
}
