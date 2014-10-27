package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.10.14
 */
public enum StaticResolveSelectors implements CSharpResolveSelector
{
	NONE
			{
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context)
				{
					return null;
				}
			},
	INDEX_METHOD_GROUP
			{
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context)
				{
					CSharpElementGroup<CSharpArrayMethodDeclaration> group = context.indexMethodGroup();
					if(group == null)
					{
						return PsiElement.EMPTY_ARRAY;
					}
					return new PsiElement[] {group};
				}
			},

	CONSTRUCTOR_GROUP
			{
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context)
				{
					CSharpElementGroup<CSharpConstructorDeclaration> group = context.constructorGroup();
					if(group == null)
					{
						return PsiElement.EMPTY_ARRAY;
					}
					return new PsiElement[] {group};
				}
			},

	DE_CONSTRUCTOR_GROUP
			{
				@NotNull
				@Override
				public PsiElement[] doSelectElement(@NotNull CSharpResolveContext context)
				{
					CSharpElementGroup<CSharpConstructorDeclaration> group = context.deConstructorGroup();
					if(group == null)
					{
						return PsiElement.EMPTY_ARRAY;
					}
					return new PsiElement[] {group};
				}
			}
}
