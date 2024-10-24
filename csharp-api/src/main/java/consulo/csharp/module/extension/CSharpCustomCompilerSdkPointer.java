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

package consulo.csharp.module.extension;

import consulo.component.util.pointer.NamedPointer;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkUtil;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleInheritableNamedPointerImpl;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08.06.15
 */
public class CSharpCustomCompilerSdkPointer extends ModuleInheritableNamedPointerImpl<Sdk>
{
	private final String myExtensionId;

	public CSharpCustomCompilerSdkPointer(@Nonnull ModuleRootLayer layer, @Nonnull String id)
	{
		super(layer, "custom-compiler-sdk");
		myExtensionId = id;
	}

	@Override
	public String getItemNameFromModule(@Nonnull Module module)
	{
		final CSharpModuleExtension<?> extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getCustomCompilerSdkPointer().getName();
		}
		return null;
	}

	@Override
	public Sdk getItemFromModule(@Nonnull Module module)
	{
		final CSharpModuleExtension<?> extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getCustomCompilerSdkPointer().get();
		}
		return null;
	}

	@Nonnull
	@Override
	public NamedPointer<Sdk> getPointer(@Nonnull ModuleRootLayer layer, @Nonnull String name)
	{
		return SdkUtil.createPointer(name);
	}

	@Override
	public Sdk getDefaultValue()
	{
		return null;
	}
}
