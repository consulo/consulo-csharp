/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.util.lang.BitUtil;
import consulo.util.lang.Comparing;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 08.11.14
 */
public class CSharpElementCompareUtil
{
	public static final int CHECK_RETURN_TYPE = 1 << 0;
	public static final int CHECK_VIRTUAL_IMPL_TYPE = 1 << 1;

	@RequiredReadAction
	public static boolean isEqual(@Nonnull PsiElement element, @Nonnull PsiElement element2)
	{
		return isEqual(element, element2, 0);
	}

	@RequiredReadAction
	public static boolean isEqualWithVirtualImpl(@Nonnull PsiElement element, @Nonnull PsiElement element2)
	{
		return isEqual(element, element2, CHECK_VIRTUAL_IMPL_TYPE);
	}

	@RequiredReadAction
	public static boolean isEqual(@Nonnull PsiElement element, @Nonnull PsiElement element2, int flags)
	{
		if(element == element2)
		{
			return true;
		}

		ProgressManager.checkCanceled();

		if(element instanceof CSharpPropertyDeclaration && element2 instanceof CSharpPropertyDeclaration)
		{
			if(!Comparing.equal(((CSharpPropertyDeclaration) element).getName(), ((CSharpPropertyDeclaration) element2).getName()))
			{
				return false;
			}

			if(!compareVirtualImpl(element, element2, flags))
			{
				return false;
			}

			if(BitUtil.isSet(flags, CHECK_RETURN_TYPE) && !CSharpTypeUtil.isTypeEqual(((CSharpPropertyDeclaration) element).toTypeRef(false), ((CSharpPropertyDeclaration) element2).toTypeRef(false)))
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

			if(!compareVirtualImpl(element, element2, flags))
			{
				return false;
			}

			if(BitUtil.isSet(flags, CHECK_RETURN_TYPE) && !CSharpTypeUtil.isTypeEqual(((CSharpEventDeclaration) element).toTypeRef(false), ((CSharpEventDeclaration) element2).toTypeRef(false)))
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
			return compareParameterList(element, element2);
		}

		if(element instanceof CSharpIndexMethodDeclaration && element2 instanceof CSharpIndexMethodDeclaration)
		{
			if(!compareVirtualImpl(element, element2, flags))
			{
				return false;
			}

			if(!compareReturnTypeRef(element, element2, flags))
			{
				return false;
			}

			return compareParameterList(element, element2);
		}

		if(element instanceof CSharpConversionMethodDeclaration && element2 instanceof CSharpConversionMethodDeclaration)
		{
			if(!CSharpTypeUtil.isTypeEqual(((CSharpConversionMethodDeclaration) element).getConversionTypeRef(), ((CSharpConversionMethodDeclaration) element2).getConversionTypeRef()))
			{
				return false;
			}
			if(!CSharpTypeUtil.isTypeEqual(((CSharpConversionMethodDeclaration) element).getReturnTypeRef(), ((CSharpConversionMethodDeclaration) element2).getReturnTypeRef()))
			{
				return false;
			}
			return compareParameterList(element, element2);
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

			if(!compareReturnTypeRef(element, element2, flags))
			{
				return false;
			}

			if(!compareVirtualImpl(element, element2, flags))
			{
				return false;
			}

			return compareParameterList(element, element2);
		}
		return false;
	}

	@RequiredReadAction
	private static boolean compareReturnTypeRef(@Nonnull PsiElement o1, @Nonnull PsiElement o2, int flags)
	{
		if(!BitUtil.isSet(flags, CHECK_RETURN_TYPE))
		{
			return true;
		}
		DotNetTypeRef returnTypeRef1 = ((DotNetLikeMethodDeclaration) o1).getReturnTypeRef();
		DotNetTypeRef returnTypeRef2 = ((DotNetLikeMethodDeclaration) o2).getReturnTypeRef();
		return CSharpTypeUtil.isTypeEqual(returnTypeRef1, returnTypeRef2);
	}

	@RequiredReadAction
	private static boolean compareVirtualImpl(@Nonnull PsiElement o1, @Nonnull PsiElement o2, int flags)
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
		return CSharpTypeUtil.isTypeEqual(((DotNetVirtualImplementOwner) o1).getTypeRefForImplement(), ((DotNetVirtualImplementOwner) o2).getTypeRefForImplement());
	}

	@RequiredReadAction
	private static boolean compareParameterList(@Nonnull PsiElement listOwner, @Nonnull PsiElement listOwner2)
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

			if(!CSharpTypeUtil.isTypeEqual(parameterTypeRef, parameterTypeRef1))
			{
				return false;
			}
		}
		return true;
	}
}
