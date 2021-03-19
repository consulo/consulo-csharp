package consulo.csharp.lang.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import consulo.annotation.access.RequiredReadAction;
import consulo.localize.LocalizeValue;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public interface CSharpReferenceWithValidation extends PsiReference
{
	@Nonnull
	@RequiredReadAction
	default LocalizeValue getErrorMessage(@Nonnull PsiElement element)
	{
		return LocalizeValue.localizeTODO("Not resolved");
	}
}
