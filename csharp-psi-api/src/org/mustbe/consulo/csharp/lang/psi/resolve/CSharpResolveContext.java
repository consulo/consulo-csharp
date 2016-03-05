package org.mustbe.consulo.csharp.lang.psi.resolve;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
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
	CSharpResolveContext EMPTY = new BaseCSharpResolveContext();

	@Nullable
	CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep);

	@Nullable
	CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup();

	@Nullable
	CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup();

	@Nullable
	CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep);

	/**
	 * @param typeRef is {@link CSharpStaticTypeRef#IMPLICIT} or {@link CSharpStaticTypeRef#EXPLICIT}
	 */
	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef, boolean deep);

	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name);

	@RequiredReadAction
	boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor);

	@NotNull
	@RequiredReadAction
	PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder);

	@RequiredReadAction
	boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep);

	@NotNull
	PsiElement getElement();
}
