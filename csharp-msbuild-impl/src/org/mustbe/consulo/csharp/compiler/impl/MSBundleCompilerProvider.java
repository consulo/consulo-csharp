package org.mustbe.consulo.csharp.compiler.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerProvider;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerUtil;
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.msbuild.bundle.MSBuildBundleType;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public class MSBundleCompilerProvider extends CSharpCompilerProvider
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

	@Override
	public void setupCompiler(@NotNull DotNetModuleExtension<?> netExtension,
			@NotNull CSharpModuleExtension<?> csharpExtension,
			@NotNull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException
	{
		if(compilerSdkHome == null)
		{
			throw new DotNetCompileFailedException("Compiler path is not resolved");
		}

		setExecutable(csharpExtension, builder, compilerSdkHome.findFileByRelativePath("bin/" + CSharpCompilerUtil.COMPILER_NAME));
	}
}
