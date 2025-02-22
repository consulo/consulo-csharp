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

package consulo.csharp.base.compiler;

import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkType;
import consulo.csharp.compiler.CSharpCompilerProvider;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.module.ui.awt.SdkComboBox;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 01.01.2016
 */
public abstract class BaseInternalCompilerProvider extends CSharpCompilerProvider
{
	public abstract String getExtensionId();

	@Nonnull
	public abstract Image getIcon();

	@Nullable
	@Override
	public SdkType getBundleType(@Nonnull DotNetSimpleModuleExtension<?> moduleExtension)
	{
		return null;
	}

	@Override
	public boolean isSelected(@Nonnull DotNetSimpleModuleExtension<?> moduleExtension, @Nonnull String name, @Nullable Sdk sdk)
	{
		return moduleExtension.getId().equals(getExtensionId()) && name.equals(CSharpModuleExtension.INTERNAL_SDK_KEY);
	}

	@Override
	public void insertCustomSdkItems(@Nullable DotNetSimpleModuleExtension extension, @Nonnull SdkComboBox comboBox)
	{
		if(extension == null)
		{
			return;
		}

		if(extension.getId().equals(getExtensionId()))
		{
			Sdk sdk = extension.getSdk();
			if(sdk == null)
			{
				return;
			}

			VirtualFile homeDirectory = sdk.getHomeDirectory();
			if(homeDirectory == null)
			{
				return;
			}

			VirtualFile child = homeDirectory.findChild(CSharpCompilerUtil.COMPILER_NAME);
			if(child != null)
			{
				comboBox.insertCustomSdkItem(CSharpModuleExtension.INTERNAL_SDK_KEY, "<internal>", getIcon());
			}
		}
	}
}
