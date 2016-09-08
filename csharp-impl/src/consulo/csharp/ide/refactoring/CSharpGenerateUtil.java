package consulo.csharp.ide.refactoring;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpTokens;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class CSharpGenerateUtil
{
	public static void normalizeBraces(@NotNull CSharpBodyWithBraces targetForGenerate)
	{
		if(targetForGenerate.getLeftBrace() == null)
		{
			targetForGenerate.getNode().addLeaf(CSharpTokens.LBRACE, "{", null);
		}

		if(targetForGenerate.getRightBrace() == null)
		{
			targetForGenerate.getNode().addLeaf(CSharpTokens.RBRACE, "}", null);
		}
	}
}
