package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

	@Nullable
	@Override
	public PsiElement doSelectElement(@NotNull CSharpResolveContext context)
	{
		return context.findOperatorGroupByTokenType(myToken);
	}
}
