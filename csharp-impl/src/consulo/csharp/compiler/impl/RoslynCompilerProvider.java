package consulo.csharp.compiler.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.compiler.CSharpCompilerProvider;
import consulo.csharp.compiler.CSharpCompilerUtil;
import consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.dotnet.sdk.RoslynBundleType;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public class RoslynCompilerProvider extends CSharpCompilerProvider
{
	@Nullable
	@Override
	public SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension)
	{
		return RoslynBundleType.getInstance();
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

		setExecutable(csharpExtension, builder, compilerSdkHome.findFileByRelativePath(CSharpCompilerUtil.COMPILER_NAME));
	}
}
