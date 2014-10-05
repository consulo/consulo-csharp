package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public interface CSharpTypeResolveContext
{
	@Nullable
	CSharpElementGroup indexMethodGroup();

	@Nullable
	CSharpElementGroup constructorGroup();

	@Nullable
	CSharpElementGroup deConstructorGroup();

	@Nullable
	CSharpElementGroup findOperatorGroupByTokenType(@NotNull IElementType type);

	@Nullable
	CSharpElementGroup findExtensionMethodByName(@NotNull String name);

	@Nullable
	PsiElement findByName(@NotNull String name, boolean deep);
}
