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
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import org.mustbe.consulo.csharp.module.extension.BaseCSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import com.intellij.openapi.roots.ModuleRootLayer;

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
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder()
	{
		return createCompilerOptionsBuilderImpl(this);
	}

	@NotNull
	public static DotNetCompilerOptionsBuilder createCompilerOptionsBuilderImpl(BaseCSharpModuleExtension<?> extension)
	{
		MSBaseDotNetCompilerOptionsBuilder optionsBuilder = new MSBaseDotNetCompilerOptionsBuilder(extension);
		optionsBuilder.addArgument("/fullpaths");
		optionsBuilder.addArgument("/utf8output");

		switch(extension.getLanguageVersion())
		{
			case _1_0:
				optionsBuilder.addArgument("/langversion:ISO-1");
				break;
			case _2_0:
				optionsBuilder.addArgument("/langversion:ISO-2");
				break;
			case _3_0:
				optionsBuilder.addArgument("/langversion:3");
				break;
			case _4_0:
			case _5_0:
				optionsBuilder.addArgument("/langversion:default");
				break;
		}
		optionsBuilder.setExecutableFromSdk("csc.exe");
		return optionsBuilder;
	}
}
