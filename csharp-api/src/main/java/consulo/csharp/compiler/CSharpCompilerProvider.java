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

package consulo.csharp.compiler;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ExtensionAPI;
import consulo.component.extension.ExtensionPointName;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.module.ui.awt.SdkComboBox;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
@ExtensionAPI(ComponentScope.APPLICATION)
public abstract class CSharpCompilerProvider
{
	public static final ExtensionPointName<CSharpCompilerProvider> EP_NAME = ExtensionPointName.create(CSharpCompilerProvider.class);

	@Nullable
	public abstract SdkType getBundleType(@Nonnull DotNetSimpleModuleExtension<?> moduleExtension);

	public void insertCustomSdkItems(@Nullable DotNetSimpleModuleExtension extension, @Nonnull SdkComboBox comboBox)
	{
	}

	public abstract void setupCompiler(@Nonnull DotNetModuleExtension<?> netExtension,
			@Nonnull CSharpModuleExtension<?> csharpExtension,
			@Nonnull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException;

	protected final void setExecutable(CSharpModuleExtension cSharpModuleExtension, DotNetCompilerOptionsBuilder builder, @Nullable VirtualFile executable) throws DotNetCompileFailedException
	{
		if(executable == null)
		{
			throw new DotNetCompileFailedException("Compiler is not resolved");
		}

		cSharpModuleExtension.setCompilerExecutable(builder, executable);
	}

	public boolean isSelected(@Nonnull DotNetSimpleModuleExtension<?> moduleExtension, @Nonnull String name, @Nullable Sdk sdk)
	{
		return sdk != null && getBundleType(moduleExtension) == sdk.getSdkType();
	}
}
