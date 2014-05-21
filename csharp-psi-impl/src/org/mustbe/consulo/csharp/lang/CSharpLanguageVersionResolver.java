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

package org.mustbe.consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.LanguageVersionResolver;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpLanguageVersionResolver implements LanguageVersionResolver<CSharpLanguage>
{
	@NotNull
	@Override
	public LanguageVersion<CSharpLanguage> getLanguageVersion(@NotNull Language language, @Nullable PsiElement element)
	{
		if(element == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(element);
		if(moduleForPsiElement == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		CSharpModuleExtension extension = ModuleUtilCore.getExtension(moduleForPsiElement, CSharpModuleExtension.class);
		if(extension == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		return CSharpLanguageVersionHelper.getInstance().getWrapper(extension.getLanguageVersion());
	}

	@Override
	public LanguageVersion<CSharpLanguage> getLanguageVersion(@NotNull Language language, @Nullable Project project, @Nullable VirtualFile
			virtualFile)
	{
		if(project  == null || virtualFile == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		Module moduleForPsiElement = ModuleUtilCore.findModuleForFile(virtualFile, project);
		if(moduleForPsiElement == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		CSharpModuleExtension extension = ModuleUtilCore.getExtension(moduleForPsiElement, CSharpModuleExtension.class);
		if(extension == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		return CSharpLanguageVersionHelper.getInstance().getWrapper(extension.getLanguageVersion());
	}
}
