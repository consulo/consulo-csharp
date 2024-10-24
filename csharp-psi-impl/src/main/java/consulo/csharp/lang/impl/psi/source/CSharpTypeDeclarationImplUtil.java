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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayUtil;
import consulo.util.lang.Comparing;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpTypeDeclarationImplUtil
{
	@RequiredReadAction
	public static boolean isInheritOrSelf(@Nonnull DotNetTypeRef typeRef, @Nonnull PsiElement scope, @Nonnull String... vmQNames)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return false;
		}

		return isInheritOrSelf0((DotNetTypeDeclaration) typeResolveResultElement, vmQNames);
	}

	@RequiredReadAction
	private static boolean isInheritOrSelf0(DotNetTypeDeclaration typeDeclaration, String... vmQNames)
	{
		if(ArrayUtil.contains(typeDeclaration.getVmQName(), vmQNames))
		{
			return true;
		}

		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length > 0)
		{
			for(DotNetTypeRef dotNetType : anExtends)
			{
				PsiElement psiElement = dotNetType.resolve().getElement();
				if(psiElement instanceof DotNetTypeDeclaration)
				{
					if(psiElement.isEquivalentTo(typeDeclaration))
					{
						return false;
					}

					if(ArrayUtil.contains(((DotNetTypeDeclaration) psiElement).getVmQName(), vmQNames))
					{
						return true;
					}

					if(isInheritOrSelf0((DotNetTypeDeclaration) psiElement, vmQNames))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@RequiredReadAction
	public static boolean isEquivalentTo(@Nonnull DotNetTypeDeclaration thisType, @Nullable PsiElement another)
	{
		// faster check that string
		if(thisType == another)
		{
			return true;
		}

		return another instanceof DotNetTypeDeclaration && Comparing.equal(thisType.getVmQName(), ((DotNetTypeDeclaration) another).getVmQName());
	}

	public static boolean hasExtensions(@Nonnull DotNetTypeDeclaration typeDeclaration)
	{
		for(DotNetNamedElement qualifiedElement : typeDeclaration.getMembers())
		{
			if(qualifiedElement instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) qualifiedElement).isExtension())
			{
				return true;
			}
		}
		return false;
	}

	@RequiredReadAction
	@Nonnull
	public static DotNetTypeRef[] getExtendTypeRefs(@Nonnull DotNetTypeDeclaration typeDeclaration)
	{
		DotNetTypeRef[] typeRefs = DotNetTypeRef.EMPTY_ARRAY;
		DotNetTypeList extendList = typeDeclaration.getExtendList();
		if(extendList != null && !typeDeclaration.isEnum())
		{
			typeRefs = extendList.getTypeRefs();
		}

		if(typeRefs.length == 0)
		{
			String defaultSuperType = getDefaultSuperType(typeDeclaration);
			if(defaultSuperType == null)
			{
				return DotNetTypeRef.EMPTY_ARRAY;
			}
			typeRefs = new DotNetTypeRef[]{new CSharpTypeRefByQName(typeDeclaration, defaultSuperType)};
		}
		return typeRefs;
	}

	@Nullable
	@RequiredReadAction
	public static Pair<DotNetTypeDeclaration, DotNetGenericExtractor> resolveBaseType(@Nonnull DotNetTypeDeclaration typeDeclaration, @Nonnull PsiElement scope)
	{
		typeDeclaration = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(typeDeclaration);

		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length != 0)
		{
			for(DotNetTypeRef anExtend : anExtends)
			{
				DotNetTypeResolveResult resolveResult = anExtend.resolve();
				PsiElement resolve = resolveResult.getElement();
				if(resolve instanceof DotNetTypeDeclaration && !((DotNetTypeDeclaration) resolve).isInterface())
				{
					return Pair.create((DotNetTypeDeclaration) resolve, resolveResult.getGenericExtractor());
				}
			}
		}

		String defaultSuperType = getDefaultSuperType(typeDeclaration);
		if(defaultSuperType != null)
		{
			DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(typeDeclaration.getProject()).findType(defaultSuperType, scope.getResolveScope(), CSharpTransform.INSTANCE);
			if(type != null)
			{
				return Pair.create(type, DotNetGenericExtractor.EMPTY);
			}
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public static String getDefaultSuperType(@Nonnull DotNetTypeDeclaration typeDeclaration)
	{
		String vmQName = typeDeclaration.getVmQName();
		if(Comparing.equal(vmQName, DotNetTypes.System.Object))
		{
			return null;
		}
		if(typeDeclaration.isStruct())
		{
			return DotNetTypes.System.ValueType;
		}
		else if(typeDeclaration.isEnum())
		{
			return DotNetTypes.System.Enum;
		}
		else
		{
			return DotNetTypes.System.Object;
		}
	}
}
