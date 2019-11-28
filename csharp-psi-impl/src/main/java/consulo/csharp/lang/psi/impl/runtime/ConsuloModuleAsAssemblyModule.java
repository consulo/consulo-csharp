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

import com.intellij.openapi.module.Module;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.module.DotNetAssemblyUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 14-Jun-17
 */
class ConsuloModuleAsAssemblyModule implements AssemblyModule
{
	private final Module myModule;

	ConsuloModuleAsAssemblyModule(Module module)
	{
		myModule = module;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getName()
	{
		String assemblyTitle = DotNetAssemblyUtil.getAssemblyTitle(myModule);
		return ObjectUtil.notNull(assemblyTitle, myModule.getName());
	}

	@RequiredReadAction
	@Override
	public boolean isAllowedAssembly(@Nonnull String assemblyName)
	{
		return AssemblyModuleCache.getInstance(myModule.getProject()).getSourceAllowedAssemblies(myModule).contains(assemblyName);
	}

	@Override
	public boolean equals(@Nonnull AssemblyModule module)
	{
		return module instanceof ConsuloModuleAsAssemblyModule && myModule.equals(((ConsuloModuleAsAssemblyModule) module).myModule);
	}
}
