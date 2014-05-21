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
import com.intellij.lang.Language;
import com.intellij.lang.LanguageVersionResolvers;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.psi.templateLanguages.TemplateDataElementType;
import com.intellij.psi.templateLanguages.TemplateLanguageFileViewProvider;
import com.intellij.psi.tree.IElementType;

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
			/*DotNetModuleExtension extension = ModuleUtilCore.getExtension(file, DotNetModuleExtension.class);
			if(extension != null)
			{
				assert file.getOriginalFile() == file;
				disabledBlocks = CSharpFileStubElementType.collectDisabledBlocks(file.getOriginalFile(), extension);
			}     */
			return languageVersion.createLexer(disabledBlocks);
		}
	};
}
