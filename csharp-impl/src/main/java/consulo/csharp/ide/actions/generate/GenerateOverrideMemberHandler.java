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

package consulo.csharp.ide.actions.generate;

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class GenerateOverrideMemberHandler extends GenerateImplementOrOverrideMemberHandler
{
	@Nonnull
	@Override
	public String getTitle()
	{
		return "Choose Members For Override";
	}

	@RequiredReadAction
	@Override
	public void appendAdditionalModifiers(@Nonnull StringBuilder builder, @Nonnull PsiElement item)
	{
		CSharpModifier requiredOverrideModifier = OverrideUtil.getRequiredOverrideModifier((DotNetModifierListOwner) item);
		if(requiredOverrideModifier != null)
		{
			builder.append(requiredOverrideModifier.getPresentableText()).append(" ");
		}
	}

	@RequiredReadAction
	@Override
	public void appendReturnStatement(@Nonnull StringBuilder builder, @Nonnull PsiElement item)
	{
		generateReturn(builder, item);
	}

	@RequiredReadAction
	public static void generateReturn(@Nonnull StringBuilder builder, @Nonnull PsiElement item)
	{
		if(item instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) item;
			DotNetTypeRef returnTypeRef = methodDeclaration.getReturnTypeRef();
			if(DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, DotNetTypes.System.Void))
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
		else if(item instanceof CSharpPropertyDeclaration || item instanceof CSharpIndexMethodDeclaration)
		{
			GenerateImplementMemberHandler.generateReturn(builder, item);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> getItems(@Nonnull CSharpTypeDeclaration typeDeclaration)
	{
		Collection<PsiElement> allMembers = OverrideUtil.getAllMembers(typeDeclaration, typeDeclaration.getResolveScope(), DotNetGenericExtractor.EMPTY, false, true);

		boolean isInterface = typeDeclaration.isInterface();

		List<PsiElement> elements = new ArrayList<>();
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
