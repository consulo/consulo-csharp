package consulo.csharp.lang.psi.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.msil.MsilElementWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.smartPointers.SmartPointerAnchorProvider;

/**
 * @author VISTALL
 * @since 04.11.2015
 */
public class CSharpAnchorProvider implements SmartPointerAnchorProvider
{
	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement getAnchor(@NotNull PsiElement element)
	{
		if(element instanceof MsilElementWrapper || !element.isPhysical())
		{
			return null;
		}
		if(element instanceof CSharpTypeDeclaration ||
				element instanceof CSharpFieldDeclaration ||
				element instanceof CSharpMethodDeclaration ||
				element instanceof CSharpConstructorDeclaration ||
				element instanceof CSharpIndexMethodDeclaration ||
				//element instanceof CSharpConversionMethodDeclaration ||
				element instanceof CSharpPropertyDeclaration ||
				element instanceof CSharpEventDeclaration)
		{
			return ((PsiNameIdentifierOwner) element).getNameIdentifier();
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement restoreElement(@NotNull PsiElement anchor)
	{
		return anchor.getParent();
	}
}
