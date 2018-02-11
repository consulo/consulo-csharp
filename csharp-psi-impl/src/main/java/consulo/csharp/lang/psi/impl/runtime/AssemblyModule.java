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

package consulo.csharp.lang.psi.impl.runtime;

import javax.annotation.Nonnull;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.dll.DotNetModuleFileType;
import consulo.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 14-Jun-17
 */
public interface AssemblyModule
{
	@Nonnull
	@RequiredReadAction
	static AssemblyModule resolve(@Nonnull PsiElement element)
	{
		Module module = ModuleUtilCore.findModuleForPsiElement(element);
		if(module != null)
		{
			return new ConsuloModuleAsAssemblyModule(module);
		}

		VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
		if(virtualFile != null)
		{
			VirtualFile rootFile = ArchiveVfsUtil.getVirtualFileForArchive(virtualFile);
			if(rootFile != null && rootFile.getFileType() == DotNetModuleFileType.INSTANCE)
			{
				return new DotNetModuleAsAssemblyModule(element.getProject(), rootFile);
			}
		}
		return UnknownAssemblyModule.INSTANCE;
	}

	@Nonnull
	@RequiredReadAction
	String getName();

	@RequiredReadAction
	boolean isAllowedAssembly(@Nonnull String assemblyName);

	boolean equals(@Nonnull AssemblyModule module);
}
