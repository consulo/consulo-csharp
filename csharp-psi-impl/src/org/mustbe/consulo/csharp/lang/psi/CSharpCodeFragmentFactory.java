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
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.SharingParsingHelpers;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpCodeFragmentImpl;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightVirtualFile;
import lombok.val;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class CSharpCodeFragmentFactory
{
	private static IFileElementType TYPE_FRAGMENT = new IFileElementType(CSharpLanguage.INSTANCE)
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

			CSharpBuilderWrapper wrapper = new CSharpBuilderWrapper(builder);
			PsiBuilder.Marker mark = builder.mark();
			SharingParsingHelpers.parseType(wrapper, SharingParsingHelpers.BracketFailPolicy.NOTHING, false);
			mark.done(this);

			return builder.getTreeBuilt().getFirstChildNode();
		}
	};

	@NotNull
	public static DotNetType createType(@NotNull PsiElement scope, @NotNull String text)
	{
		CSharpCodeFragmentImpl fragment = createFragment(scope, text);
		return PsiTreeUtil.getChildOfType(fragment, DotNetType.class);
	}


	@NotNull
	public static CSharpCodeFragmentImpl createFragment(@NotNull PsiElement scope, @NotNull String text)
	{
		val virtualFile = new LightVirtualFile("dummy.cs", CSharpFileType.INSTANCE, text, System.currentTimeMillis());
		val viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(scope.getProject()), virtualFile, false);

		return new CSharpCodeFragmentImpl(TYPE_FRAGMENT, scope, viewProvider);
	}
}
