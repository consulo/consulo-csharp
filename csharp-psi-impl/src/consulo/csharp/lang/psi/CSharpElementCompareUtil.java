package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 08.11.14
 */
public class CSharpElementCompareUtil
{
	public static final int CHECK_RETURN_TYPE = 1 << 0;
	public static final int CHECK_VIRTUAL_IMPL_TYPE = 1 << 1;

	@RequiredReadAction
	public static boolean isEqual(@NotNull PsiElement element, @NotNull PsiElement element2, @NotNull PsiElement scope)
	{
		return isEqual(element, element2, 0, scope);
	}

	@RequiredReadAction
	public static boolean isEqualWithVirtualImpl(@NotNull PsiElement element, @NotNull PsiElement element2, @NotNull PsiElement scope)
	{
		return isEqual(element, element2, CHECK_VIRTUAL_IMPL_TYPE, scope);
	}

	@RequiredReadAction
	public static boolean isEqual(@NotNull PsiElement element, @NotNull PsiElement element2, int flags, @NotNull PsiElement scope)
	{
		if(element == element2)
		{
			return true;
		}

		if(element instanceof CSharpPropertyDeclaration && element2 instanceof CSharpPropertyDeclaration)
		{
			if(!Comparing.equal(((CSharpPropertyDeclaration) element).getName(), ((CSharpPropertyDeclaration) element2).getName()))
			{
				return false;
			}

			if(!compareVirtualImpl(element, element2, flags, scope))
			{
				return false;
			}

			if(BitUtil.isSet(flags, CHECK_RETURN_TYPE) && !CSharpTypeUtil.isTypeEqual(((CSharpPropertyDeclaration) element).toTypeRef(false), ((CSharpPropertyDeclaration) element2).toTypeRef(false),
					scope))
			{
				return false;
			}

			return true;
		}

		if(element instanceof CSharpEventDeclaration && element2 instanceof CSharpEventDeclaration)
		{
			if(!Comparing.equal(((CSharpEventDeclaration) element).getName(), ((CSharpEventDeclaration) element2).getName()))
			{
				return false;
			}

			if(!compareVirtualImpl(element, element2, flags, scope))
			{
				return false;
			}

			if(BitUtil.isSet(flags, CHECK_RETURN_TYPE) && !CSharpTypeUtil.isTypeEqual(((CSharpEventDeclaration) element).toTypeRef(false), ((CSharpEventDeclaration) element2).toTypeRef(false),
					scope))
			{
				return false;
			}

			return true;
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
			if(((CSharpConstructorDeclaration) element).hasModifier(DotNetModifier.STATIC) != ((CSharpConstructorDeclaration) element2).hasModifier(DotNetModifier.STATIC))
			{
				return false;
			}

			if(((CSharpConstructorDeclaration) element).isDeConstructor() != ((CSharpConstructorDeclaration) element2).isDeConstructor())
			{
				return false;
			}
			return compareParameterList(element, element2, scope);
		}

		if(element instanceof CSharpIndexMethodDeclaration && element2 instanceof CSharpIndexMethodDeclaration)
		{
			if(!compareVirtualImpl(element, element2, flags, scope))
			{
				return false;
			}

			if(!compareReturnTypeRef(element, element2, flags, scope))
			{
				return false;
			}

			return compareParameterList(element, element2, scope);
		}

		if(element instanceof CSharpConversionMethodDeclaration && element2 instanceof CSharpConversionMethodDeclaration)
		{
			if(!CSharpTypeUtil.isTypeEqual(((CSharpConversionMethodDeclaration) element).getConversionTypeRef(), ((CSharpConversionMethodDeclaration) element2).getConversionTypeRef(), scope))
			{
				return false;
			}
			if(!CSharpTypeUtil.isTypeEqual(((CSharpConversionMethodDeclaration) element).getReturnTypeRef(), ((CSharpConversionMethodDeclaration) element2).getReturnTypeRef(), scope))
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

			if(!Comparing.equal(((CSharpMethodDeclaration) element).getName(), ((CSharpMethodDeclaration) element2).getName()))
			{
				return false;
			}

			if(!compareReturnTypeRef(element, element2, flags, scope))
			{
				return false;
			}

			if(!compareVirtualImpl(element, element2, flags, scope))
			{
				return false;
			}

			return compareParameterList(element, element2, scope);
		}
		return false;
	}

	@RequiredReadAction
	private static boolean compareReturnTypeRef(@NotNull PsiElement o1, @NotNull PsiElement o2, int flags, @NotNull PsiElement scope)
	{
		if(!BitUtil.isSet(flags, CHECK_RETURN_TYPE))
		{
			return true;
		}
		DotNetTypeRef returnTypeRef1 = ((DotNetLikeMethodDeclaration) o1).getReturnTypeRef();
		DotNetTypeRef returnTypeRef2 = ((DotNetLikeMethodDeclaration) o2).getReturnTypeRef();
		return CSharpTypeUtil.isTypeEqual(returnTypeRef1, returnTypeRef2, scope);
	}

	@RequiredReadAction
	private static boolean compareVirtualImpl(@NotNull PsiElement o1, @NotNull PsiElement o2, int flags, @NotNull PsiElement scope)
	{
		if(!BitUtil.isSet(flags, CHECK_VIRTUAL_IMPL_TYPE))
		{
			return true;
		}
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
		// we need call getTypeRefForImplement() due light element have ref for original DotNetType but getTypeRefForImplement() ill return another
		return CSharpTypeUtil.isTypeEqual(((DotNetVirtualImplementOwner) o1).getTypeRefForImplement(), ((DotNetVirtualImplementOwner) o2).getTypeRefForImplement(), scope);
	}

	@RequiredReadAction
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
