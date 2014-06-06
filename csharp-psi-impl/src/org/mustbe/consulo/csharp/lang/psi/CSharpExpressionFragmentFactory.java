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

package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpExpressionFragmentFileImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.testFramework.LightVirtualFile;
import lombok.val;

/**
 * @author VISTALL
 * @since 17.04.14
 */
public class CSharpExpressionFragmentFactory
{
	private static final PsiParser ourParser = new PsiParser()
	{
		@NotNull
		@Override
		public ASTNode parse(
				@NotNull IElementType elementType, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
		{
			PsiBuilder.Marker mark = builder.mark();
			ExpressionParsing.parse(new CSharpBuilderWrapper(builder));
			mark.done(elementType);
			return builder.getTreeBuilt();
		}
	};

	private static final IFileElementType ourFileElementType = new IFileElementType("CSHARP_CODE_FRAGMENT", CSharpLanguage.INSTANCE)
	{
		@Override
		protected ASTNode doParseContents(@NotNull final ASTNode chameleon, @NotNull final PsiElement psi)
		{
			final Project project = psi.getProject();
			final Language languageForParser = getLanguageForParser(psi);
			final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
			final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
			final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion,
					chameleon.getChars());

			return ourParser.parse(this, builder, languageVersion).getFirstChildNode();
		}
	};

	public static CSharpExpressionFragmentFileImpl createExpressionFragment(Project project, String text, @Nullable PsiElement context)
	{
		val virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		val viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, true);

		CSharpExpressionFragmentFileImpl file = new CSharpExpressionFragmentFileImpl(ourFileElementType, ourFileElementType,
				viewProvider, context);
		viewProvider.forceCachedPsi(file);
		file.getNode();
		return file;
	}
}
