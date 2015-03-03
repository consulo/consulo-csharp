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
				SharedParsingHelpers.parseType(new CSharpBuilderWrapper(builder, languageVersion), SharedParsingHelpers.VAR_SUPPORT);
				while(!builder.eof())
				{
					builder.error("Unexpected token");
					builder.advanceLexer();
				}
				mark.done(elementType);
				return builder.getTreeBuilt();
			}
		};

		@Override
		protected ASTNode doParseContents(@NotNull final ASTNode chameleon, @NotNull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser,
					languageForParser.getVersions()[0],
					chameleon.getChars());
			return myParser.parse(this, builder, languageForParser.getVersions()[0]);
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
			return new CSharpDocFragmentHolder(this, text);
		}
	};

	IElementType EXPRESSION = new ILazyParseableElementType("REFERENCE", CSharpDocLanguage.INSTANCE)
	{
		private final PsiParser myParser = new PsiParser()
		{
			@NotNull
			@Override
			public ASTNode parse(@NotNull IElementType elementType, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
			{
				PsiBuilder.Marker mark = builder.mark();
				ExpressionParsing.parse(new CSharpBuilderWrapper(builder, languageVersion));
				while(!builder.eof())
				{
					builder.error("Unexpected token");
					builder.advanceLexer();
				}
				mark.done(elementType);
				return builder.getTreeBuilt();
			}
		};

		@Override
		protected ASTNode doParseContents(@NotNull final ASTNode chameleon, @NotNull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser,
					languageForParser.getVersions()[0],
					chameleon.getChars());
			return myParser.parse(this, builder, languageForParser.getVersions()[0]);
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
			return new CSharpDocFragmentHolder(this, text);
		}
	};
}
