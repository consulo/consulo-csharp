package org.mustbe.consulo.csharp.ide.debugger;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpExpressionFragmentFactory;
import org.mustbe.consulo.dotnet.debugger.DotNetDebuggerProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 10.04.14
 */
public class CSharpDebuggerProvider extends DotNetDebuggerProvider
{
	@NotNull
	@Override
	public FileType getSupportedFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@NotNull
	@Override
	public PsiFile createExpressionCodeFragment(
			@NotNull Project project,
			@NotNull PsiElement sourcePosition,
			@NotNull String text,
			boolean isPhysical)
	{
		return CSharpExpressionFragmentFactory.createExpressionFragment(project, text, sourcePosition);
	}
}
