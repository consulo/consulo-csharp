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

package consulo.csharp.lang.impl.psi.fragment;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.impl.parser.ModifierSet;
import consulo.csharp.lang.impl.parser.SharedParsingHelpers;
import consulo.csharp.lang.impl.parser.exp.ExpressionParsing;
import consulo.language.version.LanguageVersion;
import consulo.language.ast.IElementType;
import consulo.language.file.light.LightVirtualFile;
import consulo.language.parser.PsiBuilder;
import consulo.language.parser.PsiBuilderFactory;
import consulo.language.parser.PsiParser;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.language.psi.PsiElement;
import consulo.language.impl.file.SingleRootFileViewProvider;
import consulo.language.ast.IFileElementType;
import consulo.language.Language;
import consulo.language.ast.ASTNode;

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpFragmentFactory
{
	private static final PsiParser ourExpressionParser = new PsiParser()
	{
		@Nonnull
		@Override
		public ASTNode parse(@Nonnull IElementType elementType, @Nonnull PsiBuilder builder, @Nonnull LanguageVersion languageVersion)
		{
			PsiBuilder.Marker mark = builder.mark();
			ExpressionParsing.parse(new CSharpBuilderWrapper(builder, languageVersion), ModifierSet.EMPTY);
			while(!builder.eof())
			{
				builder.error("Unexpected token");
				builder.advanceLexer();
			}
			mark.done(elementType);
			return builder.getTreeBuilt();
		}
	};

	private static final PsiParser ourTypeParser = new PsiParser()
	{
		@Nonnull
		@Override
		public ASTNode parse(@Nonnull IElementType elementType, @Nonnull PsiBuilder builder, @Nonnull LanguageVersion languageVersion)
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

	private static final IFileElementType ourExpressionFileElementType = new IFileElementType("CSHARP_EXPRESSION_FRAGMENT_FILE", CSharpLanguage.INSTANCE)
	{
		@Override
		protected ASTNode doParseContents(@Nonnull final ASTNode chameleon, @Nonnull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion,
					chameleon.getChars());

			return ourExpressionParser.parse(this, builder, languageVersion).getFirstChildNode();
		}
	};

	private static final IFileElementType ourTypeFileElementType = new IFileElementType("CSHARP_TYPE_FRAGMENT_FILE", CSharpLanguage.INSTANCE)
	{
		@Override
		protected ASTNode doParseContents(@Nonnull final ASTNode chameleon, @Nonnull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion,
					chameleon.getChars());

			return ourTypeParser.parse(this, builder, languageVersion).getFirstChildNode();
		}
	};

	@Nonnull
	public static CSharpFragmentFileImpl createExpressionFragment(Project project, String text, @Nullable final PsiElement context)
	{
		LightVirtualFile virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, true)
		{
			@Nonnull
			@Override
			public SingleRootFileViewProvider createCopy(@Nonnull final VirtualFile copy)
			{
				SingleRootFileViewProvider provider = new SingleRootFileViewProvider(getManager(), copy, false);
				CSharpFragmentFileImpl psiFile = new CSharpFragmentFileImpl(ourExpressionFileElementType, ourExpressionFileElementType, provider, context);
				provider.forceCachedPsi(psiFile);
				psiFile.getNode();
				return provider;
			}
		};
		CSharpFragmentFileImpl file = new CSharpFragmentFileImpl(ourExpressionFileElementType, ourExpressionFileElementType, viewProvider, context);
		viewProvider.forceCachedPsi(file);
		file.getNode();
		return file;
	}

	@Nonnull
	public static CSharpFragmentFileImpl createTypeFragment(Project project, String text, @Nullable final PsiElement context)
	{
		LightVirtualFile virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, true);

		CSharpFragmentFileImpl file = new CSharpFragmentFileImpl(ourTypeFileElementType, ourTypeFileElementType, viewProvider, context);
		viewProvider.forceCachedPsi(file);
		file.getNode();
		return file;
	}
}
