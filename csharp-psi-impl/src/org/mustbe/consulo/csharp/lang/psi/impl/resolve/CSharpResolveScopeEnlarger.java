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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ResolveScopeEnlarger;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.psi.search.SearchScope;

/**
 * @author VISTALL
 * @since 19.12.13.
 */
public class CSharpResolveScopeEnlarger extends ResolveScopeEnlarger
{
	@Nullable
	@Override
	public SearchScope getAdditionalResolveScope(@NotNull VirtualFile virtualFile, Project project)
	{
		if(virtualFile.getFileType() == CSharpFileType.INSTANCE)
		{
			Module moduleForFile = ModuleUtilCore.findModuleForFile(virtualFile, project);
			if(moduleForFile != null)
			{
				DotNetModuleExtension extension = ModuleUtilCore.getExtension(moduleForFile, DotNetModuleExtension.class);
				if(extension != null && extension.isAllowSourceRoots())
				{
					return null;
				}
				return GlobalSearchScopes.directoryScope(project, moduleForFile.getModuleDir(), true);
			}
		}
		return null;
	}
}
