package org.mustbe.consulo.csharp.compiler;

import java.util.List;

import org.consulo.module.extension.ModuleInheritableNamedPointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.compiler.DotNetCompileFailedException;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkTable;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.projectRoots.SdkTypeId;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 11.03.2015
 */
public class CSharpCompilerUtil
{
	public static final String COMPILER_NAME = "csc.exe";

	@Nullable
	public static VirtualFile findCompilerInSdk(@NotNull DotNetModuleExtension<?> dotNetModuleExtension,
			@NotNull CSharpModuleExtension<?> cSharpModuleExtension) throws DotNetCompileFailedException
	{
		ModuleInheritableNamedPointer<Sdk> customCompilerSdkPointer = cSharpModuleExtension.getCustomCompilerSdkPointer();
		if(customCompilerSdkPointer.isNull())
		{
			return null;
		}
		else
		{
			Sdk sdk = customCompilerSdkPointer.get();
			if(sdk == null)
			{
				return null;
			}

			CSharpCompilerBundleTypeProvider provider = findProvider(dotNetModuleExtension, sdk.getSdkType());
			if(provider == null)
			{
				return null;
			}
			VirtualFile homeDirectory = sdk.getHomeDirectory();
			if(homeDirectory == null)
			{
				return null;
			}
			return provider.findCompiler(homeDirectory, COMPILER_NAME);
		}
	}

	@Nullable
	public static VirtualFile findDefaultCompilerFromProvilders(@NotNull DotNetModuleExtension dotNetModuleExtension)
	{
		SdkTable sdkTable = SdkTable.getInstance();

		for(CSharpCompilerBundleTypeProvider typeProvider : CSharpCompilerBundleTypeProvider.EP_NAME.getExtensions())
		{
			SdkType bundleType = typeProvider.getBundleType(dotNetModuleExtension);
			if(bundleType != null)
			{
				List<Sdk> sdksOfType = sdkTable.getSdksOfType(bundleType);
				for(Sdk sdk : sdksOfType)
				{
					VirtualFile homeDirectory = sdk.getHomeDirectory();
					if(homeDirectory == null)
					{
						continue;
					}
					VirtualFile child = typeProvider.findCompiler(homeDirectory, COMPILER_NAME);
					if(child != null)
					{
						return child;
					}
				}
			}
		}
		return null;
	}

	@Nullable
	private static CSharpCompilerBundleTypeProvider findProvider(@NotNull DotNetModuleExtension<?> dotNetModuleExtension, @NotNull SdkTypeId type)
	{
		for(CSharpCompilerBundleTypeProvider typeProvider : CSharpCompilerBundleTypeProvider.EP_NAME.getExtensions())
		{
			SdkType bundleType = typeProvider.getBundleType(dotNetModuleExtension);
			if(bundleType == type)
			{
				return typeProvider;
			}
		}
		return null;
	}
}
