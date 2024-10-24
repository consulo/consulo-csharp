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

package consulo.csharp.lang.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.CSharpLanguageVersionHelper;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.language.Language;
import consulo.language.psi.PsiElement;
import consulo.language.util.ModuleUtilCore;
import consulo.language.version.LanguageVersion;
import consulo.language.version.LanguageVersionResolver;
import consulo.module.Module;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
@ExtensionImpl
public class CSharpLanguageVersionResolver implements LanguageVersionResolver
{
	@RequiredReadAction
	@Nonnull
	@Override
	public LanguageVersion getLanguageVersion(@Nonnull Language language, @Nullable PsiElement element)
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
		CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(moduleForPsiElement, CSharpSimpleModuleExtension.class);
		if(extension == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		return CSharpLanguageVersionHelper.getInstance().getWrapper(extension.getLanguageVersion());
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public LanguageVersion getLanguageVersion(@Nonnull Language language, @Nullable Project project, @Nullable VirtualFile virtualFile)
	{
		if(project == null || virtualFile == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		Module moduleForPsiElement = ModuleUtilCore.findModuleForFile(virtualFile, project);
		if(moduleForPsiElement == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		CSharpSimpleModuleExtension extension = ModuleUtilCore.getExtension(moduleForPsiElement, CSharpSimpleModuleExtension.class);
		if(extension == null)
		{
			return CSharpLanguageVersionHelper.getInstance().getHighestVersion();
		}
		return CSharpLanguageVersionHelper.getInstance().getWrapper(extension.getLanguageVersion());
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
