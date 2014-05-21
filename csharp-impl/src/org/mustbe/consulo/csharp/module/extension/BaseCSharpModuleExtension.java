/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.module.CSharpConfigurationLayer;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleLangExtension;
import org.mustbe.consulo.module.extension.ChildLayeredModuleExtensionImpl;
import org.mustbe.consulo.module.extension.ConfigurationLayer;
import org.mustbe.consulo.module.extension.LayeredModuleExtension;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class BaseCSharpModuleExtension<T extends BaseCSharpModuleExtension<T>> extends ChildLayeredModuleExtensionImpl<T> implements
		DotNetModuleLangExtension<T>, CSharpModuleExtension<T>
{
	public BaseCSharpModuleExtension(@NotNull String id, @NotNull ModifiableRootModel module)
	{
		super(id, module);
	}

	public boolean isAllowUnsafeCode()
	{
		CSharpConfigurationLayer currentLayer = (CSharpConfigurationLayer) getCurrentLayer();
		return currentLayer.isAllowUnsafeCode();
	}

	public void setLanguageVersion(@NotNull CSharpLanguageVersion languageVersion)
	{
		CSharpConfigurationLayer currentLayer = (CSharpConfigurationLayer) getCurrentLayer();
		currentLayer.setLanguageVersion(languageVersion);
	}

	@NotNull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		CSharpConfigurationLayer currentLayer = (CSharpConfigurationLayer) getCurrentLayer();
		return currentLayer.getLanguageVersion();
	}

	@NotNull
	@Override
	public Class<? extends LayeredModuleExtension> getHeadClass()
	{
		return DotNetModuleExtension.class;
	}

	@NotNull
	@Override
	protected ConfigurationLayer createLayer()
	{
		return new CSharpConfigurationLayer(getProject(), getId());
	}

	@NotNull
	@Override
	public LanguageFileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}
