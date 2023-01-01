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

package consulo.csharp.mono.module.extension;

import consulo.content.bundle.Sdk;
import consulo.csharp.base.compiler.CSharpCompilerUtil;
import consulo.csharp.base.module.extension.BaseCSharpModuleExtension;
import consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.mono.dotnet.sdk.MonoSdkType;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
public class MonoCSharpModuleExtension extends BaseCSharpModuleExtension<MonoCSharpModuleExtension>
{
	public MonoCSharpModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer module)
	{
		super(id, module);
	}

	@Override
	public void setCompilerExecutable(@Nonnull DotNetCompilerOptionsBuilder builder, @Nonnull VirtualFile executable)
	{
		DotNetSimpleModuleExtension extension = getModuleRootLayer().getExtension(DotNetSimpleModuleExtension.class);
		if(extension == null)
		{
			super.setCompilerExecutable(builder, executable);
			return;
		}

		Sdk sdk = extension.getSdk();
		if(sdk == null)
		{
			super.setCompilerExecutable(builder, executable);
			return;
		}

		MSBaseDotNetCompilerOptionsBuilder msBuilder = (MSBaseDotNetCompilerOptionsBuilder) builder;

		msBuilder.setExecutable(MonoSdkType.getInstance().getExecutable(sdk));
		msBuilder.addProgramArgument(executable.getPath());
	}

	@Nonnull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder() throws DotNetCompileFailedException
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
		CSharpCompilerUtil.setupCompiler(extension, this, optionsBuilder);
		return optionsBuilder;
	}
}
