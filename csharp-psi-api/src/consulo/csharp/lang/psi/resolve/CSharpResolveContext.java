package consulo.csharp.lang.psi.resolve;

import consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.Processor;
import consulo.csharp.lang.CSharpCastType;

/**
 * @author VISTALL
 * @since 29.09.14
 */
@ArrayFactoryFields
public interface CSharpResolveContext
{
	CSharpResolveContext EMPTY = new CSharpResolveContextAdapter();

	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep);

	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup();

	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup();

	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type, boolean deep);

	@Nullable
	@RequiredReadAction
	CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull CSharpCastType castType, boolean deep);

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
