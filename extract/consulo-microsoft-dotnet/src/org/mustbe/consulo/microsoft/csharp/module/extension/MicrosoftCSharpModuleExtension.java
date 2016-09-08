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

package org.mustbe.consulo.microsoft.csharp.module.extension;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerUtil;
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
public class MicrosoftCSharpModuleExtension extends BaseCSharpModuleExtension<MicrosoftCSharpModuleExtension>
{
	public MicrosoftCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
	}

	@NotNull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder() throws DotNetCompileFailedException
	{
		MSBaseDotNetCompilerOptionsBuilder optionsBuilder = new MSBaseDotNetCompilerOptionsBuilder();
		optionsBuilder.addArgument("/fullpaths");
		optionsBuilder.addArgument("/utf8output");

		String compilerTarget = getCompilerTarget();
		if(compilerTarget != null)
		{
			optionsBuilder.addArgument("/langversion:" + compilerTarget);
		}

		DotNetModuleExtension extension = getModuleRootLayer().getExtension(DotNetModuleExtension.class);
		assert extension != null;
		Sdk sdk = extension.getSdk();
		if(sdk == null)
		{
			throw new DotNetCompileFailedException(".NET SDK is not set");
		}

		CSharpCompilerUtil.setupCompiler(extension, this, optionsBuilder);
		return optionsBuilder;
	}
}
