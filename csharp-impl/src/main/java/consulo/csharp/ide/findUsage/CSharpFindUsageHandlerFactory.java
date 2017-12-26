package consulo.csharp.ide.findUsage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	public boolean canFindUsages(@NotNull PsiElement element)
	{
		return element.getLanguage() == CSharpLanguage.INSTANCE;
	}

	@Nullable
	@Override
	public FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages)
	{
		return new CSharpFindUsageHandler(element);
	}
}
