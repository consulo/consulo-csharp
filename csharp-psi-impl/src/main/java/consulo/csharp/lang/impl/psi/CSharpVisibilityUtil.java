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
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.assembly.AssemblyModule;
import consulo.dotnet.assembly.AssemblyModuleService;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.SmartList;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 08.03.14
 */
public class CSharpVisibilityUtil
{
	@RequiredReadAction
	public static boolean isVisible(@Nonnull DotNetModifierListOwner target, @Nonnull PsiElement place)
	{
		return isVisible(target, place, CSharpAccessModifier.findModifierOrDefault(target));
	}

	@RequiredReadAction
	private static boolean isVisible(DotNetModifierListOwner target, PsiElement place, CSharpAccessModifier accessModifier)
	{
		switch(accessModifier)
		{
			case PUBLIC:
			case NONE:
				return true;
			case PROTECTED_INTERNAL:
				return isVisible(target, place, CSharpAccessModifier.INTERNAL);
			case INTERNAL:
				AssemblyModuleService assemblyModuleService = AssemblyModuleService.getInstance(place.getProject());

				AssemblyModule targetModule = assemblyModuleService.resolve(target);
				AssemblyModule placeModule = assemblyModuleService.resolve(place);

				if(targetModule.equals(placeModule))
				{
					return true;
				}

				String placeAssemblyName = placeModule.getName();

				return targetModule.isAllowedAssembly(placeAssemblyName);
			case PROTECTED:
			{
				List<DotNetTypeDeclaration> targetTypes = collectAllTypes(target);
				List<DotNetTypeDeclaration> placeTypes = collectAllTypes(place);

				for(DotNetTypeDeclaration placeType : placeTypes)
				{
					final DotNetTypeDeclaration type = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(placeType);
					for(DotNetTypeDeclaration targetType : targetTypes)
					{
						String vmQName = targetType.getVmQName();
						assert vmQName != null;
						if(DotNetInheritUtil.isParentOrSelf(vmQName, type, true))
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

	@Nonnull
	@RequiredReadAction
	private static List<DotNetTypeDeclaration> collectAllTypes(@Nonnull PsiElement place)
	{
		List<DotNetTypeDeclaration> typeDeclarations = new SmartList<>();
		if(place instanceof CSharpTypeDeclaration)
		{
			typeDeclarations.add((DotNetTypeDeclaration) place);
		}
		PsiElement type = place;
		while((type = PsiTreeUtil.getContextOfType(type, DotNetTypeDeclaration.class)) != null)
		{
			typeDeclarations.add((DotNetTypeDeclaration) type);
		}
		return typeDeclarations;
	}
}
