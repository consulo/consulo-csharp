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

package org.mustbe.consulo.microsoft.csharp.compiler;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.compiler.BaseInternalCompilerProvider;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerUtil;
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.microsoft.dotnet.sdk.MicrosoftDotNetSdkType;

/**
 * @author VISTALL
 * @since 01.01.2016
 */
public class MicrosoftInternalCompilerProvider extends BaseInternalCompilerProvider
{
	@Override
	public String getExtensionId()
	{
		return "microsoft-dotnet";
	}

	@Override
	public Icon getIcon()
	{
		return MicrosoftDotNetSdkType.getInstance().getIcon();
	}

	@Override
	public void setupCompiler(@NotNull DotNetModuleExtension<?> netExtension,
			@NotNull CSharpModuleExtension<?> csharpExtension,
			@NotNull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException
	{
		VirtualFile sdkHome = netExtension.getSdk() == null ? null : netExtension.getSdk().getHomeDirectory();
		if(sdkHome == null)
		{
			throw new DotNetCompileFailedException(".NET sdk path is not resolved");
		}

		setExecutable(csharpExtension, builder, sdkHome.findFileByRelativePath(CSharpCompilerUtil.COMPILER_NAME));
	}
}
