package org.mustbe.consulo.csharp.lang.psi.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public interface CSharpElementGroup<T extends PsiElement> extends PsiNamedElement
{
	@NotNull
	Collection<T> getElements();

	@Override
	@NotNull
	String getName();
}
