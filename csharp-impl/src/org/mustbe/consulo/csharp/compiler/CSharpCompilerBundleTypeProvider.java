package org.mustbe.consulo.csharp.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public interface CSharpCompilerBundleTypeProvider
{
	ExtensionPointName<CSharpCompilerBundleTypeProvider> EP_NAME = ExtensionPointName.create("org.mustbe.consulo.csharp.compilerBundleTypeProvider");

	@Nullable
	SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension);

	@Nullable
	VirtualFile findCompiler(@NotNull VirtualFile homeDirectory, @NotNull String fileName);
}
