/*
 * Copyright 2013-2022 consulo.io
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

package consulo.csharp.microsoft.module.extension;

import consulo.annotation.component.ExtensionImpl;
import consulo.dotnet.microsoft.icon.MicrosoftDotNetIconGroup;
import consulo.localize.LocalizeValue;
import consulo.module.content.layer.ModuleExtensionProvider;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.extension.ModuleExtension;
import consulo.module.extension.MutableModuleExtension;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11-Sep-22
 */
@ExtensionImpl
public class MicrosoftCSharpModuleExtensionProvider implements ModuleExtensionProvider<MicrosoftCSharpModuleExtension>
{
	@Nonnull
	@Override
	public String getId()
	{
		return "microsoft-csharp";
	}

	@Nullable
	@Override
	public String getParentId()
	{
		return "microsoft-dotnet";
	}

	@Nonnull
	@Override
	public LocalizeValue getName()
	{
		return LocalizeValue.localizeTODO("C#");
	}

	@Nonnull
	@Override
	public Image getIcon()
	{
		return MicrosoftDotNetIconGroup.dotnet();
	}

	@Nonnull
	@Override
	public ModuleExtension<MicrosoftCSharpModuleExtension> createImmutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MicrosoftCSharpModuleExtension(getId(), moduleRootLayer);
	}

	@Nonnull
	@Override
	public MutableModuleExtension<MicrosoftCSharpModuleExtension> createMutableExtension(@Nonnull ModuleRootLayer moduleRootLayer)
	{
		return new MicrosoftCSharpMutableModuleExtension(getId(), moduleRootLayer);
	}
}
