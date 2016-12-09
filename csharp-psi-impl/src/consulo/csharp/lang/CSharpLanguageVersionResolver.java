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

package consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.Language;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.lang.LanguageVersion;
import consulo.lang.LanguageVersionResolver;

/**
 * @author VISTALL
 * @since 22.11.13.
 */
public class CSharpLanguageVersionResolver implements LanguageVersionResolver
{
	@RequiredReadAction
	@NotNull
	@Override
	public LanguageVersion getLanguageVersion(@NotNull Language language, @Nullable PsiElement element)
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

	@NotNull
	@RequiredReadAction
	@Override
	public LanguageVersion getLanguageVersion(@NotNull Language language, @Nullable Project project, @Nullable VirtualFile virtualFile)
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
}
