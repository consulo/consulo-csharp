/*
 * Copyright 2013 must-be.org
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

package org.mustbe.consulo.mono.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerUtil;
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.mono.dotnet.sdk.MonoSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
public class MonoCSharpModuleExtension extends BaseCSharpModuleExtension<MonoCSharpModuleExtension>
{
	public MonoCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@NotNull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder()
	{
		MSBaseDotNetCompilerOptionsBuilder optionsBuilder = new MSBaseDotNetCompilerOptionsBuilder();

		String compilerTarget = getCompilerTarget();
		if(compilerTarget != null)
		{
			optionsBuilder.addArgument("/langversion:" + compilerTarget);
		}

		DotNetModuleExtension extension = getModuleRootLayer().getExtension(DotNetModuleExtension.class);
		assert extension != null;
		Sdk sdk = extension.getSdk();
		assert sdk != null;

		VirtualFile compilerInSdk = CSharpCompilerUtil.findCompilerInSdk(sdk, "csc.exe");
		if(compilerInSdk != null)
		{
			optionsBuilder.setExecutable(MonoSdkType.getInstance().getExecutable(sdk));
			optionsBuilder.addArgument(compilerInSdk.getPath());
		}
		else
		{
			if(SystemInfo.isWindows)
			{
				optionsBuilder.setExecutableFromSdk(sdk, "/../../../bin/mcs.bat");
			}
			else if(SystemInfo.isMac)
			{
				optionsBuilder.setExecutableFromSdk(sdk, "/../../../bin/mcs");
			}
			else if(SystemInfo.isLinux)
			{
				optionsBuilder.setExecutable(MonoSdkType.LINUX_COMPILER);
			}
		}

		return optionsBuilder;
	}
}
