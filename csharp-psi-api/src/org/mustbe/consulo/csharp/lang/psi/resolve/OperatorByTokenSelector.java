package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
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

	@NotNull
	@Override
	public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context)
	{
		CSharpElementGroup<CSharpMethodDeclaration> groupByTokenType = context.findOperatorGroupByTokenType(myToken);
		if(groupByTokenType == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}
		return new PsiElement[] {groupByTokenType};
	}
}
