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

package consulo.csharp.microsoft.compiler;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.base.compiler.BaseInternalCompilerProvider;
import consulo.csharp.base.compiler.CSharpCompilerUtil;
import consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.microsoft.dotnet.sdk.MicrosoftDotNetSdkType;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 01.01.2016
 */
@ExtensionImpl(id = "ms-internal", order = "first")
public class MicrosoftInternalCompilerProvider extends BaseInternalCompilerProvider
{
	@Override
	public String getExtensionId()
	{
		return "microsoft-dotnet";
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return MicrosoftDotNetSdkType.getInstance().getIcon();
	}

	@Override
	public void setupCompiler(@Nonnull DotNetModuleExtension<?> netExtension,
			@Nonnull CSharpModuleExtension<?> csharpExtension,
			@Nonnull MSBaseDotNetCompilerOptionsBuilder builder,
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
