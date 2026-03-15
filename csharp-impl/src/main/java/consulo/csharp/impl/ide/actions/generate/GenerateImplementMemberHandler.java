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

package consulo.csharp.impl.ide.actions.generate;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.editor.generation.ImplementMethodHandler;
import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.localize.LocalizeValue;

import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 16.12.14
 */
@ExtensionImpl
public class GenerateImplementMemberHandler extends GenerateImplementOrOverrideMemberHandler implements ImplementMethodHandler
{
	@Override
	public LocalizeValue getTitle()
	{
		return LocalizeValue.localizeTODO("Choose Members For Implement");
	}

	@RequiredReadAction
	@Override
	public void appendAdditionalModifiers(StringBuilder builder, PsiElement item)
	{
		CSharpModifier requiredOverrideModifier = OverrideUtil.getRequiredOverrideModifier((DotNetModifierListOwner) item);
		if(requiredOverrideModifier != null)
		{
			builder.append(requiredOverrideModifier.getPresentableText()).append(" ");
		}
	}

	@RequiredReadAction
	@Override
	public void appendReturnStatement(StringBuilder builder, PsiElement item)
	{
		generateReturn(builder, item);
	}

	@RequiredReadAction
	public static void generateReturn(StringBuilder builder, PsiElement item)
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
	private static void generateReturnForTypeRef(StringBuilder builder, DotNetTypeRef typeRef, PsiElement item)
	{
		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(typeRef);
		if(defaultValueForType != null)
		{
			builder.append("return ").append(defaultValueForType).append(";\n");
		}
	}

	@RequiredReadAction
	@Override
	public Collection<DotNetModifierListOwner> getItems(CSharpTypeDeclaration typeDeclaration)
	{
		if(typeDeclaration.isInterface())
		{
			return Collections.emptyList();
		}
		return OverrideUtil.collectMembersWithModifier(typeDeclaration, DotNetGenericExtractor.EMPTY, CSharpModifier.ABSTRACT);
	}
}
