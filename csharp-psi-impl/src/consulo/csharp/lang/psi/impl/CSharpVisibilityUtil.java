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

package consulo.csharp.lang.psi.impl;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.psi.impl.stub.index.AttributeListIndex;
import consulo.dotnet.module.DotNetAssemblyUtil;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
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
	@RequiredReadAction
	public static boolean isVisible(@NotNull DotNetModifierListOwner target, @NotNull PsiElement place)
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
				Module targetModule = ModuleUtilCore.findModuleForPsiElement(target);
				Module placeModule = ModuleUtilCore.findModuleForPsiElement(place);
				if(targetModule != null)
				{
					if(targetModule.equals(placeModule))
					{
						return true;
					}

					if(placeModule == null)
					{
						return false;
					}

					String placeAssemblyName = DotNetAssemblyUtil.getAssemblyTitle(place);
					if(placeAssemblyName == null)
					{
						return false;
					}

					List<String> allowListForInternal = findAllowListForInternal(targetModule);
					return allowListForInternal.contains(placeAssemblyName);
				}
				return false;
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

	@RequiredReadAction
	@NotNull
	public static List<String> findAllowListForInternal(@NotNull Module targetModule)
	{
		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, targetModule.getProject(), targetModule.getModuleScope());

		List<String> list = new SmartList<String>();
		for(CSharpAttributeList attributeList : attributeLists)
		{
			for(CSharpAttribute attribute : attributeList.getAttributes())
			{
				DotNetTypeDeclaration dotNetTypeDeclaration = attribute.resolveToType();
				if(dotNetTypeDeclaration == null)
				{
					continue;
				}

				if(DotNetTypes2.System.Runtime.CompilerServices.InternalsVisibleToAttribute.equalsIgnoreCase(dotNetTypeDeclaration.getVmQName()))
				{
					Module attributeModule = ModuleUtilCore.findModuleForPsiElement(attribute);
					if(attributeModule == null || !attributeModule.equals(targetModule))
					{
						continue;
					}

					DotNetExpression[] parameterExpressions = attribute.getParameterExpressions();
					if(parameterExpressions.length == 0)
					{
						continue;
					}
					String valueAs = new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
					if(valueAs != null)
					{
						list.add(valueAs);
					}
				}
			}
		}
		return list;
	}

	@NotNull
	private static List<DotNetTypeDeclaration> collectAllTypes(PsiElement place)
	{
		List<DotNetTypeDeclaration> typeDeclarations = new SmartList<DotNetTypeDeclaration>();
		PsiElement type = place;
		while((type = PsiTreeUtil.getContextOfType(type, DotNetTypeDeclaration.class)) != null)
		{
			typeDeclarations.add((DotNetTypeDeclaration) type);
		}
		return typeDeclarations;
	}
}
