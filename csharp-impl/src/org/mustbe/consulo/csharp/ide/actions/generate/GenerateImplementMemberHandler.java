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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
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

	@Override
	public void processItem(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		if(item instanceof CSharpMethodDeclaration)
		{
			if(((CSharpMethodDeclaration) item).getModifierList().hasModifierInTree(CSharpModifier.ABSTRACT))
			{
				builder.append("override ");
			}
		}
	}

	@Override
	public void processReturn(@NotNull StringBuilder builder, @NotNull PsiElement item)
	{
		if(item instanceof CSharpMethodDeclaration)
		{
			String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(((CSharpMethodDeclaration) item).getReturnTypeRef(), item);
			if(defaultValueForType != null)
			{
				builder.append("return ").append(defaultValueForType).append(";\n");
			}
		}
	}

	@NotNull
	@Override
	public Collection<PsiElement> getItems(@NotNull CSharpTypeDeclaration typeDeclaration)
	{
		return OverrideUtil.collectMembersWithModifier(typeDeclaration, DotNetGenericExtractor.EMPTY, CSharpModifier.ABSTRACT);
	}
}
