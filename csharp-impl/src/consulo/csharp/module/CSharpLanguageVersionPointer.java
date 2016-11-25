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

package consulo.csharp.module;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.module.extension.impl.ModuleInheritableNamedPointerImpl;
import consulo.roots.ModuleRootLayer;
import consulo.util.pointers.NamedPointer;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CSharpLanguageVersionPointer extends ModuleInheritableNamedPointerImpl<CSharpLanguageVersion>
{
	private final String myExtensionId;

	public CSharpLanguageVersionPointer(@NotNull ModuleRootLayer layer, @NotNull String id)
	{
		super(layer, "language-version");
		myExtensionId = id;
	}

	@Override
	public String getItemNameFromModule(@NotNull Module module)
	{
		final CSharpSimpleModuleExtension extension = (CSharpSimpleModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getLanguageVersion().getName();
		}
		return null;
	}

	@Override
	public CSharpLanguageVersion getItemFromModule(@NotNull Module module)
	{
		final CSharpSimpleModuleExtension extension = (CSharpSimpleModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getLanguageVersion();
		}
		return null;
	}

	@NotNull
	@Override
	public NamedPointer<CSharpLanguageVersion> getPointer(@NotNull ModuleRootLayer layer, @NotNull String name)
	{
		return CSharpLanguageVersion.valueOf(name);
	}

	@Override
	public CSharpLanguageVersion getDefaultValue()
	{
		return CSharpLanguageVersion.HIGHEST;
	}
}
