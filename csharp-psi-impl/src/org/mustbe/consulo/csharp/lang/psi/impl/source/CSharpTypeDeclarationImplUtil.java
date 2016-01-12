/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpTypeDeclarationImplUtil
{
	@RequiredReadAction
	public static boolean isInheritOrSelf(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope, @NotNull String... vmQNames)
	{
		DotNetTypeResolveResult typeResolveResult = typeRef.resolve(scope);
		PsiElement typeResolveResultElement = typeResolveResult.getElement();
		if(!(typeResolveResultElement instanceof DotNetTypeDeclaration))
		{
			return false;
		}

		return isInheritOrSelf0((DotNetTypeDeclaration) typeResolveResultElement, scope, vmQNames);
	}

	@RequiredReadAction
	private static boolean isInheritOrSelf0(DotNetTypeDeclaration typeDeclaration, PsiElement scope, String... vmQNames)
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
				PsiElement psiElement = dotNetType.resolve(scope).getElement();
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

					if(isInheritOrSelf0((DotNetTypeDeclaration) psiElement, scope, vmQNames))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@RequiredReadAction
	public static boolean isEquivalentTo(@NotNull DotNetTypeDeclaration thisType, @Nullable PsiElement another)
	{
		return another instanceof DotNetTypeDeclaration && Comparing.equal(thisType.getVmQName(), ((DotNetTypeDeclaration) another).getVmQName());
	}

	public static boolean hasExtensions(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		for(DotNetNamedElement qualifiedElement : typeDeclaration.getMembers())
		{
			if(CSharpMethodImplUtil.isExtensionMethod(qualifiedElement))
			{
				return true;
			}
		}
		return false;
	}

	@RequiredReadAction
	@NotNull
	public static DotNetTypeRef[] getExtendTypeRefs(@NotNull DotNetTypeDeclaration t)
	{
		DotNetTypeRef[] typeRefs = DotNetTypeRef.EMPTY_ARRAY;
		DotNetTypeList extendList = t.getExtendList();
		if(extendList != null && !t.isEnum())
		{
			typeRefs = extendList.getTypeRefs();
		}

		if(typeRefs.length == 0)
		{
			String defaultSuperType = getDefaultSuperType(t);
			if(defaultSuperType == null)
			{
				return DotNetTypeRef.EMPTY_ARRAY;
			}
			typeRefs = new DotNetTypeRef[]{new CSharpTypeRefByQName(defaultSuperType)};
		}
		return typeRefs;
	}

	@Nullable
	@RequiredReadAction
	public static Pair<DotNetTypeDeclaration, DotNetGenericExtractor> resolveBaseType(@NotNull DotNetTypeDeclaration typeDeclaration,
			@NotNull PsiElement scope)
	{
		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length != 0)
		{
			for(DotNetTypeRef anExtend : anExtends)
			{
				DotNetTypeResolveResult resolveResult = anExtend.resolve(scope);
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
			DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(typeDeclaration.getProject()).findType(defaultSuperType,
					scope.getResolveScope(), DotNetPsiSearcher.TypeResoleKind.UNKNOWN, CSharpTransform.INSTANCE);
			if(type != null)
			{
				return Pair.create(type, DotNetGenericExtractor.EMPTY);
			}
		}
		return null;
	}

	@Nullable
	@RequiredReadAction
	public static String getDefaultSuperType(@NotNull DotNetTypeDeclaration typeDeclaration)
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
