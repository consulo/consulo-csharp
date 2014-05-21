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
	public static void parseFieldOrLocalVariableAtTypeWithDone(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to)
	{
		if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
		{
			builder.error("Type expected");

			marker.done(to);
		}
		else
		{
			parseFieldOrLocalVariableAtNameWithDone(builder, marker, to);
		}
	}

	public static PsiBuilder.Marker parseFieldOrLocalVariableAtTypeWithRollback(CSharpBuilderWrapper builder, PsiBuilder.Marker marker,
		IElementType to)
	{
		if(parseType(builder, BracketFailPolicy.NOTHING, false) == null)
		{
			builder.error("Type expected");

			marker.rollbackTo();
			return null;
		}
		else
		{
			return parseFieldOrLocalVariableAtNameWithRollback(builder, marker, to);
		}
	}

	public static boolean parseFieldOrLocalVariableAtNameWithDone(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to)
	{
		if(builder.getTokenType() == IDENTIFIER)
		{
			builder.advanceLexer();

			parseFieldAfterName(builder, marker, to);
			return true;
		}
		else
		{
			builder.error("Name expected");

			marker.done(to);
			return false;
		}
	}

	public static PsiBuilder.Marker parseFieldOrLocalVariableAtNameWithRollback(CSharpBuilderWrapper builder, PsiBuilder.Marker marker,
			IElementType to)
	{
		if(builder.getTokenType() == IDENTIFIER)
		{
			builder.advanceLexer();

			return parseFieldAfterName(builder, marker, to);
		}
		else
		{
			builder.error("Name expected");
			marker.rollbackTo();
			return null;
		}
	}

	private static PsiBuilder.Marker parseFieldAfterName(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to)
	{
		if(builder.getTokenType() == EQ)
		{
			builder.advanceLexer();
			if(ExpressionParsing.parse(builder) == null)
			{
				builder.error("Expression expected");
			}
		}

		if(builder.getTokenType() == COMMA)
		{
			marker.done(to);

			builder.advanceLexer();

			PsiBuilder.Marker newMarker = builder.mark();

			parseFieldOrLocalVariableAtNameWithDone(builder, newMarker, to);

			return marker;
		}
		else
		{
			marker.done(to);

			return marker;
		}
	}

	public static void parseArrayAfterThis(CSharpBuilderWrapper builderWrapper, PsiBuilder.Marker marker)
	{
		if(builderWrapper.getTokenType() == LBRACKET)
		{
			MethodParsing.parseParameterList(builderWrapper, RBRACKET);

			parseAccessors(builderWrapper, XXX_ACCESSOR, PROPERTY_ACCESSOR_START);

			marker.done(ARRAY_METHOD_DECLARATION);
		}
		else
		{
			builderWrapper.error("'[' expected");
		}
	}

	public static void parseFieldOrPropertyAfterName(CSharpBuilderWrapper builderWrapper, PsiBuilder.Marker marker)
	{
		if(builderWrapper.getTokenType() == LBRACE)
		{
			parseAccessors(builderWrapper, XXX_ACCESSOR, PROPERTY_ACCESSOR_START);

			if(builderWrapper.getTokenType() == EQ)
			{
				builderWrapper.advanceLexer();
				if(ExpressionParsing.parse(builderWrapper) == null)
				{
					builderWrapper.error("Expression expected");
				}
				expect(builderWrapper, SEMICOLON, "';' expected");
			}

			marker.done(PROPERTY_DECLARATION);
		}
		else
		{
			parseFieldAfterName(builderWrapper, marker, FIELD_DECLARATION);

			expect(builderWrapper, SEMICOLON, "';' expected");
		}
	}
}
