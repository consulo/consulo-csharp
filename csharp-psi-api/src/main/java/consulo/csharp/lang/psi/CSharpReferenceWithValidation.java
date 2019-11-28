package consulo.csharp.lang.psi;

import javax.annotation.Nonnull;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import consulo.annotation.access.RequiredReadAction;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public interface CSharpReferenceWithValidation extends PsiReference
{
	@Nonnull
	@RequiredReadAction
	default String getErrorMessage(@Nonnull PsiElement element)
	{
		return "Not resolved";
	}
}
