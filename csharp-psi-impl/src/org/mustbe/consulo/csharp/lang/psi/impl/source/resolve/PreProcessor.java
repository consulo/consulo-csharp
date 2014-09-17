package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.09.14
 */
public interface PreProcessor<T extends PsiElement>
{
	@NotNull
	T transform(T element);
}
