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

package org.mustbe.consulo.csharp.lang.psi.impl;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 08.03.14
 */
public class CSharpVisibilityUtil
{
	public static boolean isVisibleForCompletion(@NotNull DotNetModifierListOwner target, @NotNull PsiElement place)
	{
		if(!isVisible(target, place))
		{
			return false;
		}

		//TODO [VISTALL] static
		return true;
	}

	public static boolean isVisible(@NotNull DotNetModifierListOwner target, @NotNull PsiElement place)
	{
		CSharpAccessModifier accessModifier = CSharpAccessModifier.PUBLIC;
		for(CSharpAccessModifier value : CSharpAccessModifier.VALUES)
		{
			if(target.hasModifier(value.toModifier()))
			{
				accessModifier = value;
				break;
			}
		}

		switch(accessModifier)
		{
			case PUBLIC:
				return true;
			case INTERNAL:
				Module targetModule = ModuleUtilCore.findModuleForPsiElement(target);
				Module placeModule = ModuleUtilCore.findModuleForPsiElement(place);
				return targetModule != null && targetModule.equals(placeModule);
			case PROTECTED:
			{
				List<DotNetTypeDeclaration> targetTypes = collectAllTypes(target);
				List<DotNetTypeDeclaration> placeTypes = collectAllTypes(place);

				for(DotNetTypeDeclaration placeType : placeTypes)
				{
					for(DotNetTypeDeclaration targetType : targetTypes)
					{
						if(placeType.isInheritor(targetType, true))
						{
							return true;
						}
					}
				}
				break;
			}
			case PRIVATE:
			{
				List<DotNetTypeDeclaration> targetTypes = collectAllTypes(target);
				List<DotNetTypeDeclaration> placeTypes = collectAllTypes(place);

				for(DotNetTypeDeclaration placeType : placeTypes)
				{
					for(DotNetTypeDeclaration type : targetTypes)
					{
						if(placeType.isEquivalentTo(type))
						{
							return true;
						}
					}
				}
				break;
			}
		}
		return false;
	}

	private static List<DotNetTypeDeclaration> collectAllTypes(PsiElement place)
	{
		List<DotNetTypeDeclaration> typeDeclarations = new SmartList<DotNetTypeDeclaration>();
		PsiElement type = place;
		while((type = PsiTreeUtil.getParentOfType(type, DotNetTypeDeclaration.class)) != null)
		{
			typeDeclarations.add((DotNetTypeDeclaration) type);
		}
		return typeDeclarations;
	}
}
