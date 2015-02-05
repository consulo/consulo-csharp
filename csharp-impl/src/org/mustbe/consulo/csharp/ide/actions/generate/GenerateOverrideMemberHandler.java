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

package org.mustbe.consulo.csharp.ide.actions.generate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class GenerateOverrideMemberHandler extends GenerateImplementOrOverrideMemberHandler
{
	@NotNull
	@Override
	public String getTitle()
	{
		return "Choose Members For Override";
	}

	@Override
	public void processItem(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
	}

	@Override
	public void processReturn(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		generateReturn(builder, item);
	}

	public static void generateReturn(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		if(item instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) item;
			DotNetTypeRef returnTypeRef = methodDeclaration.getReturnTypeRef();
			if(DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, item, DotNetTypes.System.Void))
			{
				return;
			}

			builder.append("return base.").append(methodDeclaration.getName()).append("(");
			DotNetParameter[] parameters = methodDeclaration.getParameters();
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetParameter parameter = parameters[i];
				builder.append(parameter.getName());
			}
			builder.append(");\n");
		}
	}

	@NotNull
	@Override
	public Collection<PsiElement> getItems(@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<PsiElement> allMembers = OverrideUtil.getAllMembers(typeDeclaration, typeDeclaration.getResolveScope(),
				DotNetGenericExtractor.EMPTY);

		boolean isInterface = typeDeclaration.isInterface();

		List<PsiElement> elements = new ArrayList<PsiElement>();
		for(PsiElement element : allMembers)
		{
			if(isInterface)
			{
				if(element instanceof DotNetModifierListOwner)
				{
					if(!((DotNetModifierListOwner) element).hasModifier(CSharpModifier.INTERFACE_ABSTRACT))
					{
						continue;
					}

					if(((DotNetModifierListOwner) element).hasModifier(CSharpModifier.STATIC))
					{
						continue;
					}

					if(!CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) element, typeDeclaration))
					{
						continue;
					}
				}
			}
			else
			{
				if(element instanceof DotNetModifierListOwner)
				{
					if(((DotNetModifierListOwner) element).hasModifier(DotNetModifier.ABSTRACT))
					{
						continue;
					}

					if(((DotNetModifierListOwner) element).hasModifier(DotNetModifier.STATIC))
					{
						continue;
					}

					if(!CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) element, typeDeclaration))
					{
						continue;
					}
				}
			}

			if(element instanceof CSharpMethodDeclaration)
			{
				elements.add(element);
			}
		}
		return elements;
	}
}
