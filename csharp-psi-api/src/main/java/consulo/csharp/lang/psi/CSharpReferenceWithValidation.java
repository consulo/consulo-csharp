package consulo.csharp.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.localize.LocalizeValue;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public interface CSharpReferenceWithValidation extends PsiReference
{
	@RequiredReadAction
	default LocalizeValue getErrorMessage(PsiElement element)
	{
		return LocalizeValue.localizeTODO("Not resolved");
	}
}
