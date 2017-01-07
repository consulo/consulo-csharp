package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import consulo.annotations.RequiredReadAction;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public interface CSharpReferenceWithValidation extends PsiReference
{
	@NotNull
	@RequiredReadAction
	default String getErrorMessage(@NotNull PsiElement element)
	{
		return "Not resolved";
	}
}
