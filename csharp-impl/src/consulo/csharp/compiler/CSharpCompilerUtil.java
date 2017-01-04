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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.module.extension.ModuleInheritableNamedPointer;

/**
 * @author VISTALL
 * @since 11.03.2015
 */
public class CSharpCompilerUtil
{
	public static final String COMPILER_NAME = "csc.exe";

	public static void setupCompiler(@NotNull DotNetModuleExtension<?> netExtension,
			CSharpModuleExtension<?> csharpExtension,
			MSBaseDotNetCompilerOptionsBuilder builder) throws DotNetCompileFailedException
	{
		ModuleInheritableNamedPointer<Sdk> customCompilerSdkPointer = csharpExtension.getCustomCompilerSdkPointer();
		if(customCompilerSdkPointer.isNull())
		{
			for(CSharpCompilerProvider provider : CSharpCompilerProvider.EP_NAME.getExtensions())
			{
				SdkType bundleType = provider.getBundleType(netExtension);
				if(bundleType != null)
				{
					List<Sdk> sdksOfType = SdkTable.getInstance().getSdksOfType(bundleType);
					if(!sdksOfType.isEmpty())
					{
						for(Sdk sdk : sdksOfType)
						{
							try
							{
								provider.setupCompiler(netExtension, csharpExtension, builder, sdk.getHomeDirectory());
								return;
							}
							catch(DotNetCompileFailedException ignored) // if we failed to resolved try another sdk
							{
							}
						}
					}
				}
				else if(provider.isSelected(netExtension, CSharpModuleExtension.INTERNAL_SDK_KEY, null))
				{
					try
					{
						provider.setupCompiler(netExtension, csharpExtension, builder, null);
						return;
					}
					catch(DotNetCompileFailedException ignored)  // if we failed to resolved try another provider
					{
					}
				}
			}
		}
		else
		{
			String name = customCompilerSdkPointer.getName();
			Sdk sdk = customCompilerSdkPointer.get();

			CSharpCompilerProvider provider = null;
			for(CSharpCompilerProvider it : CSharpCompilerProvider.EP_NAME.getExtensions())
			{
				if(it.isSelected(netExtension, name, sdk))
				{
					provider = it;
					break;
				}
			}

			if(provider == null)
			{
				throw new IllegalArgumentException("No available compiler");
			}

			VirtualFile homeDirectory = sdk == null ? null : sdk.getHomeDirectory();
			provider.setupCompiler(netExtension, csharpExtension, builder, homeDirectory);
		}
	}
}
