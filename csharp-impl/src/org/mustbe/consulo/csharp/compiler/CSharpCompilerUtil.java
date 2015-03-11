package org.mustbe.consulo.csharp.compiler;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.sdk.DotNetCompilerDirOrderRootType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author VISTALL
 * @since 11.03.2015
 */
public class CSharpCompilerUtil
{
	public static VirtualFile findCompilerInSdk(@NotNull Sdk sdk, @NotNull String compilerName)
	{
		VirtualFile[] files = sdk.getRootProvider().getFiles(DotNetCompilerDirOrderRootType.getInstance());

		VirtualFile compilerFile = null;

		for(VirtualFile file : files)
		{
			VirtualFile child = file.findChild(compilerName);
			if(child != null)
			{
				compilerFile = child;
				break;
			}
		}
		return compilerFile;
	}
}
