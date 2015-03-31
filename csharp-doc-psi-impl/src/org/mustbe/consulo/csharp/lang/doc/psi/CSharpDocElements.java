/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.doc.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.doc.CSharpDocLanguage;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharedParsingHelpers;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.injection.CSharpForInjectionFragmentHolder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.injection.CSharpInjectExpressionElementType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseableElement;
import com.intellij.psi.tree.ElementTypeAsPsiFactory;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public interface CSharpDocElements
{
	ElementTypeAsPsiFactory TAG = new ElementTypeAsPsiFactory("TAG", CSharpDocLanguage.INSTANCE, CSharpDocTag.class);
	ElementTypeAsPsiFactory ATTRIBUTE = new ElementTypeAsPsiFactory("ATTRIBUTE", CSharpDocLanguage.INSTANCE, CSharpDocAttribute.class);
	ElementTypeAsPsiFactory ATTRIBUTE_VALUE = new ElementTypeAsPsiFactory("ATTRIBUTE_VALUE", CSharpDocLanguage.INSTANCE,
			CSharpDocAttributeValue.class);
	ElementTypeAsPsiFactory TEXT = new ElementTypeAsPsiFactory("TEXT", CSharpDocLanguage.INSTANCE, CSharpDocText.class);

	IElementType LINE_DOC_COMMENT = new ILazyParseableElementType("LINE_DOC_COMMENT", CSharpDocLanguage.INSTANCE)
	{
		@Override
		protected Language getLanguageForParser(PsiElement psi)
		{
			return CSharpDocLanguage.INSTANCE;
		}

		@Nullable
		@Override
		public ASTNode createNode(CharSequence text)
		{
			return new LazyParseableElement(this, text);
		}
	};

	IElementType TYPE = new ILazyParseableElementType("TYPE", CSharpDocLanguage.INSTANCE)
	{
		private final PsiParser myParser = new PsiParser()
		{
			@NotNull
			@Override
			public ASTNode parse(@NotNull IElementType elementType, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
			{
				PsiBuilder.Marker mark = builder.mark();

				CSharpBuilderWrapper builderWrapper = new CSharpBuilderWrapper(builder, languageVersion);
				SharedParsingHelpers.parseType(builderWrapper, SharedParsingHelpers.VAR_SUPPORT | SharedParsingHelpers.INSIDE_DOC);

				if(builder.getTokenType() == CSharpTokens.LPAR)
				{
					mark.rollbackTo();

					mark = builder.mark();

					PsiBuilder.Marker tempMarker = builder.mark();

					ExpressionParsing.parseQualifiedReference(builderWrapper, null, SharedParsingHelpers.INSIDE_DOC, TokenSet.EMPTY);

					if(builder.getTokenType() == CSharpTokens.LPAR)
					{
						parseArgumentList(builderWrapper);
					}
					tempMarker.done(CSharpElements.METHOD_CALL_EXPRESSION);
				}

				while(!builder.eof())
				{
					builder.error("Unexpected token");
					builder.advanceLexer();
				}
				mark.done(elementType);
				return builder.getTreeBuilt();
			}
		};

		public void parseArgumentList(CSharpBuilderWrapper builder)
		{
			PsiBuilder.Marker mark = builder.mark();

			if(builder.getTokenType() != CSharpTokens.LPAR)
			{
				mark.done(CSharpElements.CALL_ARGUMENT_LIST);
				return;
			}

			builder.advanceLexer();

			if(builder.getTokenType() == CSharpTokens.RPAR)
			{
				builder.advanceLexer();
				mark.done(CSharpElements.CALL_ARGUMENT_LIST);
				return;
			}

			parseArguments(builder, CSharpTokens.RPAR);
			SharedParsingHelpers.expect(builder, CSharpTokens.RPAR, "')' expected");
			mark.done(CSharpElements.CALL_ARGUMENT_LIST);
		}

		private void parseArguments(CSharpBuilderWrapper builder, IElementType stopElement)
		{
			TokenSet stoppers = TokenSet.create(stopElement, CSharpTokens.RBRACE, CSharpTokens.SEMICOLON);

			boolean commaEntered = false;
			while(!builder.eof())
			{
				if(stoppers.contains(builder.getTokenType()))
				{
					if(commaEntered)
					{
						PsiBuilder.Marker mark = builder.mark();
						SharedParsingHelpers.emptyElement(builder, CSharpElements.ERROR_EXPRESSION);
						// call(test,)
						builder.error("Type expected");
						mark.done(CSharpElements.DOC_CALL_ARGUMENT);
					}
					break;
				}
				commaEntered = false;

				PsiBuilder.Marker argumentMarker = builder.mark();
				SharedParsingHelpers.TypeInfo marker = SharedParsingHelpers.parseType(builder, SharedParsingHelpers.VAR_SUPPORT |
						SharedParsingHelpers.INSIDE_DOC);
				if(marker == null)
				{
					PsiBuilder.Marker errorMarker = builder.mark();
					builder.advanceLexer();
					builder.error("Type expected");
					errorMarker.done(CSharpElements.ERROR_EXPRESSION);
				}
				argumentMarker.done(CSharpElements.DOC_CALL_ARGUMENT);


				if(builder.getTokenType() == CSharpTokens.COMMA)
				{
					builder.advanceLexer();
					commaEntered = true;
				}
				else if(!stoppers.contains(builder.getTokenType()))
				{
					builder.error("',' expected");
				}
			}
		}

		@Override
		protected ASTNode doParseContents(@NotNull final ASTNode chameleon, @NotNull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser,
					languageForParser.getVersions()[0], chameleon.getChars());
			return myParser.parse(this, builder, languageForParser.getVersions()[0]).getFirstChildNode();
		}

		@Override
		protected Language getLanguageForParser(PsiElement psi)
		{
			return CSharpLanguage.INSTANCE;
		}

		@Nullable
		@Override
		public ASTNode createNode(CharSequence text)
		{
			return new CSharpForInjectionFragmentHolder(this, text, CSharpReferenceExpression.ResolveToKind.TYPE_LIKE);
		}
	};

	IElementType PARAMETER_EXPRESSION = new CSharpInjectExpressionElementType("PARAMETER_REFERENCE", CSharpDocLanguage.INSTANCE,
			CSharpReferenceExpression.ResolveToKind.PARAMETER_FROM_PARENT);

	IElementType GENERIC_PARAMETER_EXPRESSION = new CSharpInjectExpressionElementType("GENERIC_PARAMETER_EXPRESSION", CSharpDocLanguage.INSTANCE,
			CSharpReferenceExpression.ResolveToKind.GENERIC_PARAMETER_FROM_PARENT);
}
