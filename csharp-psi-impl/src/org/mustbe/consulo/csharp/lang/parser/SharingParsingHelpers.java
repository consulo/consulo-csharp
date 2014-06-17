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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.util.NotNullFunction;
import lombok.val;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class SharingParsingHelpers implements CSharpTokenSets, CSharpTokens, CSharpElements
{
	public static final WhitespacesAndCommentsBinder GREEDY_RIGHT_EDGE_PROCESSOR = new WhitespacesAndCommentsBinder()
	{
		@Override
		public int getEdgePosition(final List<IElementType> tokens, final boolean atStreamEdge, final TokenTextGetter getter)
		{
			return tokens.size();
		}
	};

	public static enum BracketFailPolicy
	{
		NOTHING,
		DROP,
		RETURN_BEFORE
	}

	public static class TypeInfo
	{
		public IElementType nativeElementType;
		public boolean isParameterized;
		public PsiBuilder.Marker marker;
	}

	protected static boolean parseTypeList(@NotNull CSharpBuilderWrapper builder, boolean varSupport)
	{
		boolean empty = true;
		while(!builder.eof())
		{
			val marker = parseType(builder, BracketFailPolicy.NOTHING, varSupport);
			if(marker == null)
			{
				if(!empty)
				{
					builder.error("Type expected");
				}
				break;
			}

			empty = false;

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else
			{
				break;
			}
		}
		return empty;
	}

	public static TypeInfo parseType(@NotNull CSharpBuilderWrapper builder, BracketFailPolicy bracketFailPolicy, boolean varSupport)
	{
		TypeInfo typeInfo = parseInnerType(builder, varSupport);
		if(typeInfo == null)
		{
			return null;
		}

		PsiBuilder.Marker marker = typeInfo.marker;

		if(builder.getTokenType() == LT)
		{
			marker = marker.precede();

			typeInfo = new TypeInfo();
			typeInfo.isParameterized = true;

			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();
			if(parseTypeList(builder, varSupport))
			{
				builder.error("Type expected");
			}
			expect(builder, GT, "'>' expected");
			mark.done(TYPE_ARGUMENTS);

			marker.done(TYPE_WRAPPER_WITH_TYPE_ARGUMENTS);
		}

		while(builder.getTokenType() == MUL)
		{
			typeInfo = new TypeInfo();

			marker = marker.precede();

			builder.advanceLexer();

			marker.done(POINTER_TYPE);
		}

		while(builder.getTokenType() == LBRACKET)
		{
			PsiBuilder.Marker newMarker = marker.precede();

			if(builder.lookAhead(1) == COMMA)
			{
				builder.advanceLexer();  // advance [

				while(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}

				expect(builder, RBRACKET, "']' expected");
				newMarker.done(ARRAY_TYPE);

				typeInfo = new TypeInfo();
				marker = newMarker;
				continue;
			}

			if(builder.lookAhead(1) == RBRACKET)
			{
				builder.advanceLexer();
				builder.advanceLexer();

				newMarker.done(ARRAY_TYPE);

				typeInfo = new TypeInfo();
				marker = newMarker;
			}
			else
			{
				switch(bracketFailPolicy)
				{
					case NOTHING:
						builder.advanceLexer();  // advance [

						builder.error("']' expected");
						typeInfo = new TypeInfo();
						newMarker.done(ARRAY_TYPE);

						marker = newMarker;
						break;
					case DROP:
						newMarker.drop();
						marker.drop();
						return null;
					case RETURN_BEFORE:
						newMarker.drop();
						typeInfo.marker = marker;
						return typeInfo;
				}
			}
		}

		while(builder.getTokenType() == MUL)
		{
			typeInfo = new TypeInfo();

			marker = marker.precede();

			builder.advanceLexer();

			marker.done(POINTER_TYPE);
		}

		if(builder.getTokenType() == QUEST)
		{
			typeInfo = new TypeInfo();

			marker = marker.precede();

			builder.advanceLexer();

			marker.done(NULLABLE_TYPE);
		}

		typeInfo.marker = marker;
		return typeInfo;
	}

	private static TypeInfo parseInnerType(@NotNull CSharpBuilderWrapper builder, boolean varSupport)
	{
		TypeInfo typeInfo = new TypeInfo();

		PsiBuilder.Marker marker = builder.mark();
		if(varSupport)
		{
			builder.enableSoftKeyword(CSharpSoftTokens.VAR_KEYWORD);
		}
		IElementType tokenType = builder.getTokenType();
		if(varSupport)
		{
			builder.disableSoftKeyword(CSharpSoftTokens.VAR_KEYWORD);
		}

		typeInfo.marker = marker;
		if(CSharpTokenSets.NATIVE_TYPES.contains(tokenType))
		{
			builder.advanceLexer();
			marker.done(NATIVE_TYPE);

			typeInfo.nativeElementType = tokenType;
		}
		else if(builder.getTokenType() == IDENTIFIER)
		{
			ExpressionParsing.parseQualifiedReference(builder, null);
			marker.done(USER_TYPE);
		}
		else if(builder.getTokenType() == GLOBAL_KEYWORD)
		{
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();

			if(expect(builder, COLONCOLON, "'::' expected"))
			{
				expect(builder, IDENTIFIER, "Identifier expected");
			}
			mark.done(REFERENCE_EXPRESSION);
			marker.done(USER_TYPE);
		}
		else
		{
			marker.drop();
			return null;
		}

		return typeInfo;
	}

	protected static boolean parseAttributeList(CSharpBuilderWrapper builder)
	{
		boolean empty = true;
		while(builder.getTokenType() == LBRACKET)
		{
			empty = false;
			PsiBuilder.Marker mark = builder.mark();
			builder.advanceLexer();

			builder.enableSoftKeywords(ATTRIBUTE_TARGETS);
			IElementType tokenType = builder.getTokenType();
			builder.disableSoftKeywords(ATTRIBUTE_TARGETS);

			if(builder.lookAhead(1) != COLON)
			{
				builder.remapBackIfSoft();
			}
			else
			{
				if(!ATTRIBUTE_TARGETS.contains(tokenType))
				{
					builder.error("Wrong attribute target");
				}
				builder.advanceLexer(); // target type
				builder.advanceLexer(); // colon
			}

			while(!builder.eof())
			{
				PsiBuilder.Marker attMark = parseAttribute(builder);
				if(attMark == null)
				{
					builder.error("Attribute name expected");
					break;
				}

				if(builder.getTokenType() == COMMA)
				{
					builder.advanceLexer();
				}
				else if(builder.getTokenType() == RBRACKET)
				{
					break;
				}
			}

			expect(builder, RBRACKET, "']' expected");
			mark.done(ATTRIBUTE_LIST);
		}
		return empty;
	}

	private static PsiBuilder.Marker parseAttribute(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();
		if(ExpressionParsing.parseQualifiedReference(builder, null) == null)
		{
			mark.drop();
			return null;
		}

		if(builder.getTokenType() == LPAR)
		{
			parseAttributeParameterList(builder);
		}
		mark.done(ATTRIBUTE);
		return mark;
	}

	public static void parseAttributeParameterList(CSharpBuilderWrapper builder)
	{
		PsiBuilder.Marker mark = builder.mark();

		builder.advanceLexer();

		if(builder.getTokenType() == RPAR)
		{
			builder.advanceLexer();
			mark.done(CALL_ARGUMENT_LIST);
			return;
		}

		boolean empty = true;
		while(!builder.eof())
		{
			if(builder.getTokenType() == IDENTIFIER && builder.lookAhead(1) == EQ)
			{
				PsiBuilder.Marker marker = builder.mark();
				doneOneElement(builder, IDENTIFIER, REFERENCE_EXPRESSION, null);
				builder.advanceLexer(); // eq
				PsiBuilder.Marker expressionParser = ExpressionParsing.parse(builder);
				if(expressionParser == null)
				{
					builder.error("Expression expected");
				}
				marker.done(NAMED_CALL_ARGUMENT);
			}
			else
			{
				PsiBuilder.Marker marker = ExpressionParsing.parse(builder);
				if(marker == null)
				{
					if(!empty)
					{
						builder.error("Expression expected");
					}
					break;
				}
			}
			empty = false;

			if(builder.getTokenType() == COMMA)
			{
				builder.advanceLexer();
			}
			else if(builder.getTokenType() == RPAR)
			{
				break;
			}
			else
			{
				break;
			}
		}
		expect(builder, RPAR, "')' expected");
		mark.done(CALL_ARGUMENT_LIST);
	}

	protected static Pair<PsiBuilder.Marker, Boolean> parseModifierList(CSharpBuilderWrapper builder)
	{
		val marker = builder.mark();

		boolean empty = true;
		while(!builder.eof())
		{
			if(MODIFIERS.contains(builder.getTokenType()))
			{
				empty = false;
				builder.advanceLexer();
			}
			else
			{
				break;
			}
		}
		marker.done(MODIFIER_LIST);
		return new Pair<PsiBuilder.Marker, Boolean>(marker, empty);
	}

	protected static Pair<PsiBuilder.Marker, Boolean> parseModifierListWithAttributes(CSharpBuilderWrapper builder)
	{
		if(MODIFIERS.contains(builder.getTokenType()))
		{
			return parseModifierList(builder);
		}
		else
		{
			boolean empty = true;
			val marker = builder.mark();
			empty = parseAttributeList(builder);
			while(!builder.eof())
			{
				if(MODIFIERS.contains(builder.getTokenType()))
				{
					empty = false;
					builder.advanceLexer();
				}
				else
				{
					break;
				}
			}
			marker.done(MODIFIER_LIST);
			return new Pair<PsiBuilder.Marker, Boolean>(marker, empty);
		}
	}

	@NotNull
	protected static Pair<PsiBuilder.Marker, Boolean> parseWithSoftElements(NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, Boolean>> func,
			CSharpBuilderWrapper builderWrapper, IElementType... softs)
	{
		return parseWithSoftElements(func, builderWrapper, TokenSet.create(softs));
	}

	@NotNull
	protected static Pair<PsiBuilder.Marker, Boolean> parseWithSoftElements(NotNullFunction<CSharpBuilderWrapper, Pair<PsiBuilder.Marker, Boolean>> func,
			CSharpBuilderWrapper builderWrapper, TokenSet softs)
	{
		builderWrapper.enableSoftKeywords(softs);
		val fun = func.fun(builderWrapper);
		builderWrapper.disableSoftKeywords(softs);
		return fun;
	}

	@Nullable
	public static IElementType exprType(@Nullable final PsiBuilder.Marker marker)
	{
		return marker != null ? ((LighterASTNode) marker).getTokenType() : null;
	}

	public static boolean expect(PsiBuilder builder, IElementType elementType, String message)
	{
		if(builder.getTokenType() == elementType)
		{
			builder.advanceLexer();
			return true;
		}
		else
		{
			if(message != null)
			{
				builder.error(message);
			}
			return false;
		}
	}

	protected static boolean expect(PsiBuilder builder, TokenSet tokenSet, String message)
	{
		if(tokenSet.contains(builder.getTokenType()))
		{
			builder.advanceLexer();
			return true;
		}
		else
		{
			if(message != null)
			{
				builder.error(message);
			}
			return false;
		}
	}

	public static void emptyElement(final PsiBuilder builder, final IElementType type)
	{
		builder.mark().done(type);
	}

	protected static boolean doneOneElement(PsiBuilder builder, IElementType elementType, IElementType to, String message)
	{
		PsiBuilder.Marker mark = builder.mark();
		if(expect(builder, elementType, message))
		{
			mark.done(to);
			return true;
		}
		else
		{
			mark.drop();
			return false;
		}
	}

	public static boolean expectGGLL(CSharpBuilderWrapper builder, IElementType elementType, String message)
	{
		if(builder.getTokenTypeGGLL() == elementType)
		{
			builder.advanceLexerGGLL();
			return true;
		}
		else
		{
			if(message != null)
			{
				builder.error(message);
			}
			return false;
		}
	}

	protected static boolean doneOneElementGGLL(CSharpBuilderWrapper builder, IElementType elementType, IElementType to, String message)
	{
		PsiBuilder.Marker mark = builder.mark();
		if(expectGGLL(builder, elementType, message))
		{
			mark.done(to);
			return true;
		}
		else
		{
			mark.drop();
			return false;
		}
	}
}
