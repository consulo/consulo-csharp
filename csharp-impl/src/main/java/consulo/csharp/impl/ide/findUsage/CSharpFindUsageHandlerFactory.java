package consulo.csharp.impl.ide.findUsage;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.find.FindUsagesHandler;
import consulo.find.FindUsagesHandlerFactory;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31-Oct-17
 */
@ExtensionImpl
public class CSharpFindUsageHandlerFactory extends FindUsagesHandlerFactory
{
	@Override
	@RequiredReadAction
	public boolean canFindUsages(@Nonnull PsiElement element)
	{
		return element.getLanguage() == CSharpLanguage.INSTANCE;
	}

	@Nullable
	@Override
	public FindUsagesHandler createFindUsagesHandler(@Nonnull PsiElement element, boolean forHighlightUsages)
	{
		return new CSharpFindUsageHandler(element);
	}
}
