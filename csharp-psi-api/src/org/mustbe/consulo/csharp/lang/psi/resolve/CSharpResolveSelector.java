package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public interface CSharpResolveSelector
{
	@NotNull
	@RequiredReadAction
	PsiElement[] doSelectElement(@NotNull CSharpResolveContext context, boolean deep);
}
