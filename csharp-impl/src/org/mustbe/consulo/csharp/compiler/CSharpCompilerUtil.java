package org.mustbe.consulo.csharp.compiler;

import java.util.List;

import org.consulo.module.extension.ModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompileFailedException;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;

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
