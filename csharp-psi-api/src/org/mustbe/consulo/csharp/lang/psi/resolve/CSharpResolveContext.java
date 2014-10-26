package org.mustbe.consulo.csharp.lang.psi.resolve;

import java.util.Collection;
import java.util.Collections;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
		public CSharpElementGroup indexMethodGroup()
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup constructorGroup()
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup deConstructorGroup()
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup findOperatorGroupByTokenType(@NotNull IElementType type)
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup findConversionMethodGroup(@NotNull DotNetTypeRef typeRef)
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup findExtensionMethodGroupByName(@NotNull String name)
		{
			return null;
		}

		@NotNull
		@Override
		public Collection<CSharpElementGroup> getExtensionMethodGroups()
		{
			return Collections.emptyList();
		}

		@Nullable
		@Override
		public PsiElement findByName(@NotNull String name, @NotNull UserDataHolder holder)
		{
			return null;
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

	Key<Integer> GENERIC_COUNT = Key.create("csharp.generic.count");

	@Nullable
	CSharpElementGroup indexMethodGroup();

	@Nullable
	CSharpElementGroup constructorGroup();

	@Nullable
	CSharpElementGroup deConstructorGroup();

	@Nullable
	CSharpElementGroup findOperatorGroupByTokenType(@NotNull IElementType type);

	/**
	 * @param typeRef is {@link CSharpStaticTypeRef#IMPLICIT} or {@link CSharpStaticTypeRef#EXPLICIT}
	 */
	@Nullable
	CSharpElementGroup findConversionMethodGroup(@NotNull DotNetTypeRef typeRef);

	@Nullable
	CSharpElementGroup findExtensionMethodGroupByName(@NotNull String name);

	@NotNull
	Collection<CSharpElementGroup> getExtensionMethodGroups();

	@Nullable
	PsiElement findByName(@NotNull String name, @NotNull UserDataHolder holder);

	@NotNull
	Collection<? extends PsiElement> getElements();
}
