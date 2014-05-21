package org.mustbe.consulo.csharp.ide.debugger;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.dotnet.debugger.DotNetDebuggerProvider;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class CSharpDebuggerProvider implements DotNetDebuggerProvider
{
	@Override
	public boolean isSupported(@NotNull PsiFile psiFile)
	{
		return psiFile.getFileType() == CSharpFileType.INSTANCE;
	}
}
