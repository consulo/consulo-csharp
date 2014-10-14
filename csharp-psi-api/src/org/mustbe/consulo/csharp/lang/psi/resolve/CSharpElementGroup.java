package org.mustbe.consulo.csharp.lang.psi.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public interface CSharpElementGroup extends PsiElement
{
	@NotNull
	Collection<? extends PsiElement> getElements();
}
