package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public interface CSharpResolveSelector
{
	@Nullable
	PsiElement doSelectElement(@NotNull CSharpResolveContext context);
}
