/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.mono.csharp.compiler;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.compiler.BaseInternalCompilerProvider;
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompileFailedException;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.mono.dotnet.sdk.MonoSdkType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 01.01.2016
 */
public class MonoInternalCompilerProvider extends BaseInternalCompilerProvider
{
	@Override
	public String getExtensionId()
	{
		return "mono-dotnet";
	}

	@Override
	public Icon getIcon()
	{
		return MonoSdkType.getInstance().getIcon();
	}

	@Override
	public void setupCompiler(@NotNull DotNetModuleExtension<?> netExtension,
			@NotNull CSharpModuleExtension<?> csharpExtension,
			@NotNull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException
	{
		Sdk sdk = netExtension.getSdk();
		if(sdk == null)
		{
			throw new DotNetCompileFailedException("Mono SDK is not resolved");
		}

		if(SystemInfo.isWindows)
		{
			builder.setExecutableFromSdk(sdk, "/../../../bin/mcs.bat");
		}
		else if(SystemInfo.isMac)
		{
			builder.setExecutableFromSdk(sdk, "/../../../bin/mcs");
		}
		else if(SystemInfo.isFreeBSD)
		{
			builder.setExecutable(MonoSdkType.ourDefaultFreeBSDCompilerPath);
		}
		else if(SystemInfo.isLinux)
		{
			builder.setExecutable(MonoSdkType.ourDefaultLinuxCompilerPath);
		}
	}
}
