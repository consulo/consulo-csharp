package org.mustbe.consulo.csharp.lang.psi.resolve;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public interface CSharpElementGroup<T extends PsiElement> extends PsiElement
{
	@NotNull
	Collection<T> getElements();

	@NotNull
	String getName();
}
