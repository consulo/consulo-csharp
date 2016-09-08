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

package org.mustbe.consulo.csharp.compiler;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.roots.ui.configuration.SdkComboBox;

/**
 * @author VISTALL
 * @since 01.01.2016
 */
public abstract class BaseInternalCompilerProvider extends CSharpCompilerProvider
{
	public abstract String getExtensionId();

	public abstract Icon getIcon();

	@Nullable
	@Override
	public SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension)
	{
		return null;
	}

	@Override
	public boolean isSelected(@NotNull DotNetSimpleModuleExtension<?> moduleExtension, @NotNull String name, @Nullable Sdk sdk)
	{
		return moduleExtension.getId().equals(getExtensionId()) && name.equals(CSharpModuleExtension.INTERNAL_SDK_KEY);
	}

	@Override
	public void insertCustomSdkItems(@Nullable DotNetSimpleModuleExtension extension, @NotNull SdkComboBox comboBox)
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
