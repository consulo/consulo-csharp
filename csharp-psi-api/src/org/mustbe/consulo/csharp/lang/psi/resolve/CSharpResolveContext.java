package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;

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
		public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup(boolean deep)
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
		public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep)
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef, boolean deep)
		{
			return null;
		}

		@Nullable
		@Override
		public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
		{
			return null;
		}

		@Override
		public boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
		{
			return true;
		}

		@NotNull
		@Override
		public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
		{
			return PsiElement.EMPTY_ARRAY;
		}

		@Override
		public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
		{
			return true;
		}
	};

	@Nullable
	CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup(boolean deep);

	@Nullable
	CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup();

	@Nullable
	CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup();

	@Nullable
	CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep);

	/**
	 * @param typeRef is {@link CSharpStaticTypeRef#IMPLICIT} or {@link CSharpStaticTypeRef#EXPLICIT}
	 * @param deep
	 */
	@Nullable
	CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef, boolean deep);

	@Nullable
	CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name);

	boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor);

	@NotNull
	PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder);

	boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep);
}
