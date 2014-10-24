package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public enum StaticResolveSelectors implements CSharpResolveSelector
{
	NONE
			{
				@Nullable
				@Override
				public PsiElement doSelectElement(@NotNull CSharpResolveContext context)
				{
					return null;
				}
			},
	INDEX_METHOD_GROUP
			{
				@Nullable
				@Override
				public PsiElement doSelectElement(@NotNull CSharpResolveContext context)
				{
					return context.indexMethodGroup();
				}
			},

	CONSTRUCTOR_GROUP
			{
				@Nullable
				@Override
				public PsiElement doSelectElement(@NotNull CSharpResolveContext context)
				{
					return context.constructorGroup();
				}
			},

	DE_CONSTRUCTOR_GROUP
			{
				@Nullable
				@Override
				public PsiElement doSelectElement(@NotNull CSharpResolveContext context)
				{
					return context.deConstructorGroup();
				}
			}
}
