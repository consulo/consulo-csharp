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

import java.util.Collections;
import java.util.List;

import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.CSharpLanguageVersionWrapper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes.CSharpFileStubElementType;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageVersionResolvers;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.psi.tree.IElementType;
import com.intellij.testFramework.LightVirtualFile;
import lombok.val;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public interface CSharpTemplateTokens
{
	IElementType MACRO_FRAGMENT = new IElementType("MACRO_FRAGMENT", CSharpLanguage.INSTANCE);

	IElementType OUTER_ELEMENT_TYPE = new IElementType("OUTER_ELEMENT_TYPE", CSharpLanguage.INSTANCE);

	TemplateDataElementType TEMPLATE_DATA = new TemplateDataElementType("TEMPLATE_DATA", CSharpLanguage.INSTANCE, MACRO_FRAGMENT, OUTER_ELEMENT_TYPE)
	{
		@Override
		protected Lexer createBaseLexer(PsiFile file, TemplateLanguageFileViewProvider viewProvider)
		{
			final Language baseLanguage = viewProvider.getBaseLanguage();
			final CSharpLanguageVersionWrapper languageVersion = (CSharpLanguageVersionWrapper) LanguageVersionResolvers.INSTANCE.forLanguage
					(baseLanguage).getLanguageVersion(baseLanguage, file);

			List<TextRange> disabledBlocks = Collections.emptyList();

			Module moduleForFile = ModuleUtilCore.findModuleForFile(viewProvider.getVirtualFile(), file.getProject());

			if(moduleForFile != null)
			{
				DotNetModuleExtension extension = ModuleUtilCore.getExtension(moduleForFile, DotNetModuleExtension.class);
				if(extension != null)
				{
					CSharpMacroFileImpl macroFile = createMacroFile(moduleForFile.getProject(), viewProvider);
					disabledBlocks = CSharpFileStubElementType.collectDisabledBlocks(macroFile, extension);
				}
			}

			return languageVersion.createLexer(disabledBlocks);
		}

		private CSharpMacroFileImpl createMacroFile(Project project, TemplateLanguageFileViewProvider provider)
		{
			val virtualFile = new LightVirtualFile("dummy.cs", CSharpInnerFileType.INSTANCE, provider.getContents(), System.currentTimeMillis());
			val viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(project), virtualFile, false);
			return new CSharpMacroFileImpl(viewProvider);
		}
	};

}
