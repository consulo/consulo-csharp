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

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetXXXAccessor;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class GenerateImplementMemberHandler extends GenerateImplementOrOverrideMemberHandler
{
	@NotNull
	@Override
	public String getTitle()
	{
		return "Choose Members For Implement";
	}

	@RequiredReadAction
	@Override
	public void appendAdditionalModifiers(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		CSharpModifier requiredOverrideModifier = OverrideUtil.getRequiredOverrideModifier((DotNetModifierListOwner) item);
		if(requiredOverrideModifier != null)
		{
			builder.append(requiredOverrideModifier.getPresentableText()).append(" ");
		}
	}

	@RequiredReadAction
	@Override
	public void appendReturnStatement(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		generateReturn(builder, item);
	}

	@RequiredReadAction
	public static void generateReturn(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		if(item instanceof CSharpMethodDeclaration)
		{
			generateReturnForTypeRef(builder, ((CSharpMethodDeclaration) item).getReturnTypeRef(), item);
		}
		else if(item instanceof DotNetXXXAccessor)
		{
			DotNetXXXAccessor.Kind accessorKind = ((DotNetXXXAccessor) item).getAccessorKind();
			if(accessorKind == DotNetXXXAccessor.Kind.GET)
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
	private static void generateReturnForTypeRef(@NotNull StringBuilder builder, @NotNull DotNetTypeRef typeRef, @NotNull PsiElement item)
	{
		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(typeRef, item);
		if(defaultValueForType != null)
		{
			builder.append("return ").append(defaultValueForType).append(";\n");
		}
	}

	@RequiredReadAction
	@NotNull
	@Override
	public Collection<DotNetModifierListOwner> getItems(@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		if(typeDeclaration.isInterface())
		{
			return Collections.emptyList();
		}
		return OverrideUtil.collectMembersWithModifier(typeDeclaration, DotNetGenericExtractor.EMPTY, CSharpModifier.ABSTRACT);
	}
}
