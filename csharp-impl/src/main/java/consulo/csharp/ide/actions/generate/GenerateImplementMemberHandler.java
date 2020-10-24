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
import consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class GenerateImplementMemberHandler extends GenerateImplementOrOverrideMemberHandler
{
	@Nonnull
	@Override
	public String getTitle()
	{
		return "Choose Members For Implement";
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
			generateReturnForTypeRef(builder, ((CSharpMethodDeclaration) item).getReturnTypeRef(), item);
		}
		else if(item instanceof DotNetXAccessor)
		{
			DotNetXAccessor.Kind accessorKind = ((DotNetXAccessor) item).getAccessorKind();
			if(accessorKind == DotNetXAccessor.Kind.GET)
			{
				PsiElement parent = item.getParent();
				if(parent instanceof CSharpPropertyDeclaration)
				{
					generateReturnForTypeRef(builder, ((CSharpPropertyDeclaration) parent).toTypeRef(false), item);
				}
				else if(parent instanceof CSharpIndexMethodDeclaration)
				{
					generateReturnForTypeRef(builder, ((CSharpIndexMethodDeclaration) parent).getReturnTypeRef(), item);
				}
			}
		}
	}

	@RequiredReadAction
	private static void generateReturnForTypeRef(@Nonnull StringBuilder builder, @Nonnull DotNetTypeRef typeRef, @Nonnull PsiElement item)
	{
		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(typeRef);
		if(defaultValueForType != null)
		{
			builder.append("return ").append(defaultValueForType).append(";\n");
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<DotNetModifierListOwner> getItems(@Nonnull CSharpTypeDeclaration typeDeclaration)
	{
		if(typeDeclaration.isInterface())
		{
			return Collections.emptyList();
		}
		return OverrideUtil.collectMembersWithModifier(typeDeclaration, DotNetGenericExtractor.EMPTY, CSharpModifier.ABSTRACT);
	}
}
