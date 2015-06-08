package org.mustbe.consulo.csharp.compiler.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.compiler.CSharpCompilerBundleTypeProvider;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import org.mustbe.consulo.dotnet.sdk.RoslynBundleType;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public class RoslynCompilerBundleTypeProvider implements CSharpCompilerBundleTypeProvider
{
	@Nullable
	@Override
	public SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension)
	{
		return RoslynBundleType.getInstance();
	}

	@Nullable
	@Override
	public VirtualFile findCompiler(@NotNull VirtualFile homeDirectory, @NotNull String fileName)
	{
		return homeDirectory.findChild(fileName);
	}
}
