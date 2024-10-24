package consulo.csharp.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;

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
