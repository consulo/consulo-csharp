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

package consulo.csharp.lang.parser.decl;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.parser.exp.ExpressionParsing;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class FieldOrPropertyParsing extends MemberWithBodyParsing
{
	public static void parseFieldOrLocalVariableAtTypeWithDone(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to, int typeFlags, boolean semicolonEat, ModifierSet set)
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
			parseFieldOrLocalVariableAtNameWithDone(builder, marker, to, typeFlags, semicolonEat, set);
		}
	}

	public static boolean parseFieldOrLocalVariableAtNameWithDone(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to, int typeFlags, boolean semicolonEat, ModifierSet set)
	{
		if(expectOrReportIdentifier(builder, typeFlags))
		{
			parseFieldAfterName(builder, marker, to, typeFlags, semicolonEat, set);
			return true;
		}
		else
		{
			if(semicolonEat)
			{
				expect(builder, SEMICOLON, "';' expected");
			}

			done(marker, to);
			return false;
		}
	}

	private static PsiBuilder.Marker parseFieldAfterName(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, IElementType to, int typeFlags, boolean semicolonEat, ModifierSet set)
	{
		boolean autoProperty = builder.getTokenType() == DARROW;

		if(builder.getTokenType() == EQ || autoProperty)
		{
			builder.advanceLexer();
			if(ExpressionParsing.parseVariableInitializer(builder, set, 0) == null)
			{
				builder.error("Expression expected");
			}
		}

		if(autoProperty)
		{
			// if we found darrow - its property
			to = PROPERTY_DECLARATION;
		}

		if(!autoProperty && builder.getTokenType() == COMMA)
		{
			builder.advanceLexer();

			marker.done(to);

			PsiBuilder.Marker newMarker = builder.mark();

			parseFieldOrLocalVariableAtNameWithDone(builder, newMarker, to, typeFlags, semicolonEat, set);

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

	public static void parseArrayAfterThis(CSharpBuilderWrapper builder, PsiBuilder.Marker marker, ModifierSet set)
	{
		if(builder.getTokenType() == LBRACKET)
		{
			MethodParsing.parseParameterList(builder, STUB_SUPPORT, RBRACKET, set);
		}
		else
		{
			builder.error("'[' expected");
		}

		boolean autoProperty = builder.getTokenType() == DARROW;

		if(autoProperty)
		{
			MethodParsing.parseExpressionCodeBlock(builder);
		}
		else
		{
			parseAccessors(builder, XACCESSOR, PROPERTY_ACCESSOR_START);
		}

		done(marker, INDEX_METHOD_DECLARATION);
	}

	public static void parseFieldOrPropertyAfterName(CSharpBuilderWrapper builderWrapper, PsiBuilder.Marker marker, ModifierSet set)
	{
		if(builderWrapper.getTokenType() == LBRACE)
		{
			parseAccessors(builderWrapper, XACCESSOR, PROPERTY_ACCESSOR_START);

			if(builderWrapper.getTokenType() == EQ)
			{
				builderWrapper.advanceLexer();
				if(ExpressionParsing.parseVariableInitializer(builderWrapper, set, 0) == null)
				{
					builderWrapper.error("Expression expected");
				}
				expect(builderWrapper, SEMICOLON, "';' expected");
			}

			done(marker, PROPERTY_DECLARATION);
		}
		else
		{
			parseFieldAfterName(builderWrapper, marker, FIELD_DECLARATION, STUB_SUPPORT, true, set);
		}
	}
}
