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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.annotation.access.RequiredReadAction;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 14-Jun-17
 */
class DotNetModuleAsAssemblyModule implements AssemblyModule
{
	private final Project myProject;
	private final VirtualFile myModuleFile;

	DotNetModuleAsAssemblyModule(Project project, VirtualFile moduleFile)
	{
		myProject = project;
		myModuleFile = moduleFile;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getName()
	{
		return myModuleFile.getNameWithoutExtension();
	}

	@RequiredReadAction
	@Override
	public boolean isAllowedAssembly(@Nonnull String assemblyName)
	{
		return AssemblyModuleCache.getInstance(myProject).getBinaryAllowedAssemblies(myModuleFile).contains(assemblyName);
	}

	@Override
	public boolean equals(@Nonnull AssemblyModule module)
	{
		return module instanceof DotNetModuleAsAssemblyModule && myModuleFile.equals(((DotNetModuleAsAssemblyModule) module).myModuleFile);
	}
}
