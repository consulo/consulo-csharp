package consulo.csharp.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.module.extension.CSharpModuleExtension;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.roots.ui.configuration.SdkComboBox;

/**
 * @author VISTALL
 * @since 08.06.2015
 */
public abstract class CSharpCompilerProvider
{
	public static final ExtensionPointName<CSharpCompilerProvider> EP_NAME = ExtensionPointName.create("consulo.csharp.compilerProvider");

	@Nullable
	public abstract SdkType getBundleType(@NotNull DotNetSimpleModuleExtension<?> moduleExtension);

	public void insertCustomSdkItems(@Nullable DotNetSimpleModuleExtension extension, @NotNull SdkComboBox comboBox)
	{
	}

	public abstract void setupCompiler(@NotNull DotNetModuleExtension<?> netExtension,
			@NotNull CSharpModuleExtension<?> csharpExtension,
			@NotNull MSBaseDotNetCompilerOptionsBuilder builder,
			@Nullable VirtualFile compilerSdkHome) throws DotNetCompileFailedException;

	protected final void setExecutable(CSharpModuleExtension cSharpModuleExtension, DotNetCompilerOptionsBuilder builder, @Nullable VirtualFile executable) throws DotNetCompileFailedException
	{
		if(executable == null)
		{
			throw new DotNetCompileFailedException("Compiler is not resolved");
		}

		cSharpModuleExtension.setCompilerExecutable(builder, executable);
	}

	public boolean isSelected(@NotNull DotNetSimpleModuleExtension<?> moduleExtension, @NotNull String name, @Nullable Sdk sdk)
	{
		return sdk != null && getBundleType(moduleExtension) == sdk.getSdkType();
	}
}
