package consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public class OperatorByTokenSelector implements CSharpResolveSelector
{
	private final IElementType myToken;

	public OperatorByTokenSelector(IElementType token)
	{
		myToken = token;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep)
	{
		CSharpElementGroup<CSharpMethodDeclaration> groupByTokenType = context.findOperatorGroupByTokenType(myToken, deep);
		if(groupByTokenType == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}
		return new PsiElement[] {groupByTokenType};
	}
}
