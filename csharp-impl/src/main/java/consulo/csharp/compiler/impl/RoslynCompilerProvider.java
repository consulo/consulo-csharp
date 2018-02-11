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

package consulo.csharp.compiler.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.csharp.compiler.CSharpCompilerProvider;
import consulo.csharp.compiler.CSharpCompilerUtil;
import consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.dotnet.sdk.RoslynBundleType;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public class RoslynCompilerProvider extends CSharpCompilerProvider
{
	@Nullable
	@Override
	public SdkType getBundleType(@Nonnull DotNetSimpleModuleExtension<?> moduleExtension)
	{
		return RoslynBundleType.getInstance();
	}

	@Override
	public void setupCompiler(@Nonnull DotNetModuleExtension<?> netExtension,
			@Nonnull CSharpModuleExtension<?> csharpExtension,
			@Nonnull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException
	{
		if(compilerSdkHome == null)
		{
			throw new DotNetCompileFailedException("Compiler path is not resolved");
		}

		setExecutable(csharpExtension, builder, compilerSdkHome.findFileByRelativePath(CSharpCompilerUtil.COMPILER_NAME));
	}
}
