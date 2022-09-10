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

package consulo.csharp.lang.doc.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.impl.CSharpDocLanguage;
import consulo.csharp.lang.doc.impl.lexer.CSharpReferenceLexer;
import consulo.csharp.lang.doc.impl.lexer.DeprecatedCSharpDocLexer;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.impl.psi.CompositeElementTypeAsPsiFactory;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.injection.CSharpForInjectionFragmentHolder;
import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.ILazyParseableElementType;
import consulo.language.ast.TokenSet;
import consulo.language.parser.ParserDefinition;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiElement;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionUtil;
import consulo.project.Project;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public interface CSharpDocElements
{
	IElementType TAG = new CompositeElementTypeAsPsiFactory("TAG", CSharpDocLanguage.INSTANCE, CSharpDocTagImpl::new);
	IElementType ATTRIBUTE = new CompositeElementTypeAsPsiFactory("ATTRIBUTE", CSharpDocLanguage.INSTANCE, CSharpDocAttribute::new);
	IElementType ATTRIBUTE_VALUE = new CompositeElementTypeAsPsiFactory("ATTRIBUTE_VALUE", CSharpDocLanguage.INSTANCE, CSharpDocAttributeValue::new);
	IElementType TEXT = new CompositeElementTypeAsPsiFactory("TEXT", CSharpDocLanguage.INSTANCE, CSharpDocText::new);

	IElementType LINE_DOC_COMMENT = new ILazyParseableElementType("LINE_DOC_COMMENT", CSharpDocLanguage.INSTANCE)
	{
		@Override
		@RequiredReadAction
		protected ASTNode doParseContents(@Nonnull final ASTNode chameleon, @Nonnull final PsiElement psi)
		{
			final Project project = psi.getProject();
			CSharpDocLanguage docLanguage = CSharpDocLanguage.INSTANCE;
			final LanguageVersion languageVersion = LanguageVersionUtil.findDefaultVersion(docLanguage);
			DeprecatedCSharpDocLexer docLexer = new DeprecatedCSharpDocLexer();
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, docLexer, docLanguage, languageVersion, chameleon.getChars());
			final PsiParser parser = ParserDefinition.forLanguage(docLanguage).createParser(languageVersion);
			return parser.parse(this, builder, languageVersion).getFirstChildNode();
		}

		@Nullable
		@Override
		public ASTNode createNode(CharSequence text)
		{
			return new CSharpDocRootImpl(this, text);
		}
	};

	IElementType TYPE = new ILazyParseableElementType("TYPE", CSharpDocLanguage.INSTANCE)
	{
		private final PsiParser myParser = (elementType, builder, languageVersion) ->
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
				SharedParsingHelpers.TypeInfo marker = SharedParsingHelpers.parseType(builder, SharedParsingHelpers.VAR_SUPPORT | SharedParsingHelpers.INSIDE_DOC);
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
		protected ASTNode doParseContents(@Nonnull final ASTNode chameleon, @Nonnull final PsiElement psi)
		{
			Project project = psi.getProject();
			Language languageForParser = getLanguageForParser(psi);
			CSharpReferenceLexer lexer = new CSharpReferenceLexer();
			PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, languageForParser.getVersions()[0], chameleon.getChars());
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

	IElementType PARAMETER_EXPRESSION = new CSharpInjectExpressionElementType("PARAMETER_REFERENCE", CSharpDocLanguage.INSTANCE, CSharpReferenceExpression.ResolveToKind.PARAMETER_FROM_PARENT);

	IElementType GENERIC_PARAMETER_EXPRESSION = new CSharpInjectExpressionElementType("GENERIC_PARAMETER_EXPRESSION", CSharpDocLanguage.INSTANCE, CSharpReferenceExpression.ResolveToKind
			.GENERIC_PARAMETER_FROM_PARENT);
}
