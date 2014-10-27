package org.mustbe.consulo.csharp.lang.psi.resolve;

import java.util.Collection;
import java.util.Collections;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 29.09.14
 */
@ArrayFactoryFields
public interface CSharpResolveContext
{
	CSharpResolveContext EMPTY = new CSharpResolveContext()
	{
		@Nullable
		@Override
		public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup()
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type)
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef)
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
		{
			return null;
		}

		@NotNull
		@Override
		public Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups()
		{
			return Collections.emptyList();
		}

		@NotNull
		@Override
		public PsiElement[] findByName(@NotNull String name, @NotNull UserDataHolder holder)
		{
			return PsiElement.EMPTY_ARRAY;
		}

		@NotNull
		@Override
		public Collection<? extends PsiElement> getElements()
		{
			return Collections.emptyList();
		}

	};

	UserDataHolder EMPTY_USER_DATA = new UserDataHolder()
	{
		@Nullable
		@Override
		public <T> T getUserData(@NotNull Key<T> key)
		{
			return null;
		}

		@Override
		public <T> void putUserData(@NotNull Key<T> key, @Nullable T value)
		{

		}
	};

	@Nullable
	CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup();

	@Nullable
	CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup();

	@Nullable
	CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup();

	@Nullable
	CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type);

	/**
	 * @param typeRef is {@link CSharpStaticTypeRef#IMPLICIT} or {@link CSharpStaticTypeRef#EXPLICIT}
	 */
	@Nullable
	CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef);

	@Nullable
	CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name);

	@NotNull
	Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups();

	@NotNull
	PsiElement[] findByName(@NotNull String name, @NotNull UserDataHolder holder);

	@NotNull
	Collection<? extends PsiElement> getElements();
}
