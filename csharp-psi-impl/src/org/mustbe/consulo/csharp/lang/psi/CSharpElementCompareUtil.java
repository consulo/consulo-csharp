package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 08.11.14
 */
public class CSharpElementCompareUtil
{
	public static boolean isEqual(@NotNull PsiElement element, @NotNull PsiElement element2, @NotNull PsiElement scope)
	{
		return isEqual(element, element2, false, scope);
	}

	public static boolean isEqualWithVirtualImpl(@NotNull PsiElement element, @NotNull PsiElement element2, @NotNull PsiElement scope)
	{
		return isEqual(element, element2, true, scope);
	}

	private static boolean isEqual(@NotNull PsiElement element, @NotNull PsiElement element2, boolean checkVirtualImpl, @NotNull PsiElement scope)
	{
		if(element == element2)
		{
			return true;
		}

		if(element instanceof CSharpPropertyDeclaration && element2 instanceof CSharpPropertyDeclaration)
		{
			if(checkVirtualImpl && !compareVirtualImpl(element, element2, scope))
			{
				return false;
			}

			return Comparing.equal(((CSharpPropertyDeclaration) element).getName(), ((CSharpPropertyDeclaration) element2).getName());
		}

		if(element instanceof CSharpEventDeclaration && element2 instanceof CSharpEventDeclaration)
		{
			if(checkVirtualImpl && !compareVirtualImpl(element, element2, scope))
			{
				return false;
			}

			return Comparing.equal(((CSharpEventDeclaration) element).getName(), ((CSharpEventDeclaration) element2).getName());
		}

		if(element instanceof CSharpFieldDeclaration && element2 instanceof CSharpFieldDeclaration)
		{
			return Comparing.equal(((CSharpFieldDeclaration) element).getName(), ((CSharpFieldDeclaration) element2).getName());
		}

		if(element instanceof CSharpTypeDeclaration && element2 instanceof CSharpTypeDeclaration)
		{
			if(((CSharpTypeDeclaration) element).getGenericParametersCount() != ((CSharpTypeDeclaration) element2).getGenericParametersCount())
			{
				return false;
			}

			return Comparing.equal(((CSharpTypeDeclaration) element).getName(), ((CSharpTypeDeclaration) element2).getName());
		}

		if(element instanceof CSharpConstructorDeclaration && element2 instanceof CSharpConstructorDeclaration)
		{
			if(((CSharpConstructorDeclaration) element).hasModifier(DotNetModifier.STATIC) != ((CSharpConstructorDeclaration) element2).hasModifier
					(DotNetModifier.STATIC))
			{
				return false;
			}

			if(((CSharpConstructorDeclaration) element).isDeConstructor() != ((CSharpConstructorDeclaration) element2).isDeConstructor())
			{
				return false;
			}
			return compareParameterList(element, element2, scope);
		}

		if(element instanceof CSharpArrayMethodDeclaration && element2 instanceof CSharpArrayMethodDeclaration)
		{
			if(checkVirtualImpl && !compareVirtualImpl(element, element2, scope))
			{
				return false;
			}

			return compareParameterList(element, element2, scope);
		}

		if(element instanceof CSharpConversionMethodDeclaration && element2 instanceof CSharpConversionMethodDeclaration)
		{
			if(!CSharpTypeUtil.isTypeEqual(((CSharpConversionMethodDeclaration) element).getConversionTypeRef(),
					((CSharpConversionMethodDeclaration) element2).getConversionTypeRef(), scope))
			{
				return false;
			}
			return compareParameterList(element, element2, scope);
		}

		if(element instanceof CSharpMethodDeclaration && element2 instanceof CSharpMethodDeclaration)
		{
			if(((CSharpMethodDeclaration) element).getGenericParametersCount() != ((CSharpMethodDeclaration) element2).getGenericParametersCount())
			{
				return false;
			}

			if(checkVirtualImpl && !compareVirtualImpl(element, element2, scope))
			{
				return false;
			}

			if(!Comparing.equal(((CSharpMethodDeclaration) element).getName(), ((CSharpMethodDeclaration) element2).getName()))
			{
				return false;
			}

			return compareParameterList(element, element2, scope);
		}
		return false;
	}

	private static boolean compareVirtualImpl(@NotNull PsiElement o1, @NotNull PsiElement o2, @NotNull PsiElement scope)
	{
		DotNetType type1 = ((DotNetVirtualImplementOwner) o1).getTypeForImplement();
		DotNetType type2 = ((DotNetVirtualImplementOwner) o2).getTypeForImplement();

		if(type1 == null && type2 == null)
		{
			return true;
		}

		if(type1 == null || type2 == null)
		{
			return false;
		}
		return CSharpTypeUtil.isTypeEqual(type1.toTypeRef(), type2.toTypeRef(), scope);
	}

	private static boolean compareParameterList(@NotNull PsiElement listOwner, @NotNull PsiElement listOwner2, @NotNull PsiElement scope)
	{
		DotNetTypeRef[] parameterTypeRefs = ((DotNetParameterListOwner) listOwner).getParameterTypeRefs();
		DotNetTypeRef[] parameterTypeRefs1 = ((DotNetParameterListOwner) listOwner2).getParameterTypeRefs();
		if(parameterTypeRefs.length != parameterTypeRefs1.length)
		{
			return false;
		}

		for(int i = 0; i < parameterTypeRefs.length; i++)
		{
			DotNetTypeRef parameterTypeRef = parameterTypeRefs[i];
			DotNetTypeRef parameterTypeRef1 = parameterTypeRefs1[i];

			if(!CSharpTypeUtil.isTypeEqual(parameterTypeRef, parameterTypeRef1, scope))
			{
				return false;
			}
		}
		return true;
	}
}
