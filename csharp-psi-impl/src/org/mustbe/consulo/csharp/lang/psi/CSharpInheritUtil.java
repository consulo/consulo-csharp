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

package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 31.12.13.
 */
@Logger
public class CSharpInheritUtil
{
	public static boolean isParentOrSelf(@NotNull String parentClass, DotNetTypeRef typeRef, PsiElement element, boolean deep)
	{
		PsiElement resolve = typeRef.resolve(element);
		if(!(resolve instanceof DotNetTypeDeclaration))
		{
			return false;
		}
		DotNetTypeDeclaration typeDeclaration = (DotNetTypeDeclaration) resolve;
		return isParentOrSelf(parentClass, typeDeclaration, deep);
	}

	public static boolean isParentOrSelf(@NotNull String parentClass, DotNetTypeDeclaration typeDeclaration, boolean deep)
	{
		if(Comparing.equal(parentClass, typeDeclaration.getPresentableQName()))
		{
			return true;
		}
		return isParent(parentClass, typeDeclaration, deep);
	}

	public static boolean isParent(@NotNull String parentClass, DotNetTypeDeclaration typeDeclaration, boolean deep)
	{
		val type = DotNetPsiFacade.getInstance(typeDeclaration.getProject()).findType(parentClass, typeDeclaration.getResolveScope(), -1);
		return type != null && typeDeclaration.isInheritor(type, deep);
	}

	public static boolean isInheritor(DotNetTypeDeclaration typeDeclaration, DotNetTypeDeclaration other, boolean deep)
	{
		if(typeDeclaration.isEquivalentTo(other))
		{
			return true;
		}
		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length > 0)
		{
			for(DotNetTypeRef dotNetType : anExtends)
			{
				PsiElement psiElement = dotNetType.resolve(typeDeclaration);
				if(psiElement instanceof CSharpTypeDeclaration)
				{
					if(psiElement.isEquivalentTo(typeDeclaration))
					{
						return false;
					}

					if(deep)
					{
						if(isInheritor((DotNetTypeDeclaration) psiElement, other, true))
						{
							return true;
						}
					}
				}
			}
		}
		else
		{
			String defaultSuperType = CSharpResolveUtil.getDefaultSuperType(typeDeclaration);
			if(defaultSuperType == null)
			{
				return false;
			}

			DotNetTypeDeclaration type = DotNetPsiFacade.getInstance(typeDeclaration.getProject()).findType(defaultSuperType,
					typeDeclaration.getResolveScope(), 0);
			if(type != null)
			{
				return !type.isEquivalentTo(typeDeclaration) && isInheritor(type, other, true);
			}
		}
		return false;
	}
}
