package consulo.csharp.ide.findUsage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;

/**
 * @author VISTALL
 * @since 31-Oct-17
 */
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
