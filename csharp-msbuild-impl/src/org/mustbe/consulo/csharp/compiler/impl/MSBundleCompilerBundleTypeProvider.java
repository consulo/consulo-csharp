package org.mustbe.consulo.csharp.compiler.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerBundleTypeProvider;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import org.mustbe.consulo.msbuild.bundle.MSBuildBundleType;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public class MSBundleCompilerBundleTypeProvider implements CSharpCompilerBundleTypeProvider
{
	@Nullable
	@Override
	public SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension)
	{
		// hack - due we can known about MSBuild plugin or Microsoft .NET, double depends option file is not supported
		if(moduleExtension.getId().equals("microsoft-dotnet"))
		{
			return MSBuildBundleType.getInstance();
		}
		return null;
	}

	@Nullable
	@Override
	public VirtualFile findCompiler(@NotNull VirtualFile homeDirectory, @NotNull String fileName)
	{
		return homeDirectory.findFileByRelativePath("bin/" + fileName);
	}
}
