/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.RenameQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpPsiSearcher;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 01.11.2015
 */
public class CS0264 extends CompilerCheck<DotNetGenericParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetGenericParameter element)
	{
		DotNetGenericParameterListOwner listOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
		if(listOwner instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) listOwner).hasModifier(CSharpModifier.PARTIAL))
		{
			CSharpCompositeTypeDeclaration compositeType = findCompositeType((CSharpTypeDeclaration) listOwner);
			if(compositeType == null)
			{
				return null;
			}

			CSharpTypeDeclaration[] typeDeclarations = compositeType.getTypeDeclarations();
			for(CSharpTypeDeclaration typeDeclaration : typeDeclarations)
			{
				if(typeDeclaration == listOwner)
				{
					continue;
				}

				DotNetGenericParameter anotherGenericParameter = typeDeclaration.getGenericParameters()[element.getIndex()];
				if(!Comparing.equal(anotherGenericParameter.getName(), element.getName()))
				{
					PsiElement nameIdentifier = element.getNameIdentifier();
					assert nameIdentifier != null;
					return newBuilder(nameIdentifier, formatElement(listOwner)).addQuickFix(new RenameQuickFix(anotherGenericParameter.getName(), element));
				}
			}
		}
		return super.checkImpl(languageVersion, element);
	}


	@RequiredReadAction
	public static CSharpCompositeTypeDeclaration findCompositeType(@NotNull CSharpTypeDeclaration parent)
	{
		String vmQName = parent.getVmQName();
		assert vmQName != null;
		DotNetTypeDeclaration[] types = CSharpPsiSearcher.getInstance(parent.getProject()).findTypes(vmQName, parent.getResolveScope());

		for(DotNetTypeDeclaration type : types)
		{
			if(type instanceof CSharpCompositeTypeDeclaration)
			{
				CSharpTypeDeclaration[] typeDeclarations = ((CSharpCompositeTypeDeclaration) type).getTypeDeclarations();
				if(ArrayUtil.contains(parent, typeDeclarations))
				{
					return (CSharpCompositeTypeDeclaration) type;
				}
			}
		}

		return null;
	}
}
