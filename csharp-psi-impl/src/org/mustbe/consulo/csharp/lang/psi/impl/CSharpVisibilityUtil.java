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

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpAccessModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeList;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.AttributeListIndex;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeTargetType;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.module.impl.scopes.ModuleWithDependenciesScope;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 08.03.14
 */
public class CSharpVisibilityUtil
{
	public static boolean isVisible(@NotNull DotNetModifierListOwner target, @NotNull PsiElement place)
	{
		return isVisible(target, place, CSharpAccessModifier.findModifier(target));
	}

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

					String placeAssemblyName = findAssemblyName(placeModule);
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
		} return false;
	}

	@Nullable
	private static String findAssemblyName(@NotNull Module module)
	{
		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, module.getProject(),
				new ModuleWithDependenciesScope(module, 0));

		loop:for(CSharpAttributeList attributeList : attributeLists)
		{
			for(CSharpAttribute attribute : attributeList.getAttributes())
			{
				DotNetTypeDeclaration dotNetTypeDeclaration = attribute.resolveToType();
				if(dotNetTypeDeclaration == null)
				{
					continue;
				}
				if(DotNetTypes.System.Reflection.AssemblyTitleAttribute.equalsIgnoreCase(dotNetTypeDeclaration.getVmQName()))
				{
					DotNetExpression[] parameterExpressions = attribute.getParameterExpressions();
					if(parameterExpressions.length == 0)
					{
						break loop;
					}
					String valueAs = new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
					if(valueAs != null)
					{
						return valueAs;
					}
				}
			}
		}
		return module.getName();
	}

	@NotNull
	public static List<String> findAllowListForInternal(@NotNull Module targetModule)
	{
		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, targetModule.getProject(),
				new ModuleWithDependenciesScope(targetModule, 0));

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
