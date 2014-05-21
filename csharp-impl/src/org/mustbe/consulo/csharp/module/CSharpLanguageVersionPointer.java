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

package org.mustbe.consulo.csharp.module;

import org.consulo.module.extension.impl.ModuleInheritableNamedPointerImpl;
import org.consulo.util.pointers.NamedPointer;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CSharpLanguageVersionPointer extends ModuleInheritableNamedPointerImpl<CSharpLanguageVersion>
{
	private final String myExtensionId;

	public CSharpLanguageVersionPointer(@NotNull Project project, @NotNull String id)
	{
		super(project, "language-version");
		myExtensionId = id;
	}

	@Override
	public String getItemNameFromModule(@NotNull Module module)
	{
		final CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getLanguageVersion().getName();
		}
		return null;
	}

	@Override
	public CSharpLanguageVersion getItemFromModule(@NotNull Module module)
	{
		final CSharpModuleExtension extension = (CSharpModuleExtension) ModuleUtilCore.getExtension(module, myExtensionId);
		if(extension != null)
		{
			return extension.getLanguageVersion();
		}
		return null;
	}

	@NotNull
	@Override
	public NamedPointer<CSharpLanguageVersion> getPointer(@NotNull Project project, @NotNull String name)
	{
		return CSharpLanguageVersion.valueOf(name);
	}

	@Override
	public CSharpLanguageVersion getDefaultValue()
	{
		return CSharpLanguageVersion.HIGHEST;
	}
}
