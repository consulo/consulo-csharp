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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.ide.highlight.quickFix.RenameQuickFix;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 01.11.2015
 */
public class CS0264 extends CompilerCheck<DotNetGenericParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetGenericParameter element)
	{
		DotNetGenericParameterListOwner listOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
		if(listOwner instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) listOwner).hasModifier(CSharpModifier.PARTIAL))
		{
			CSharpCompositeTypeDeclaration compositeType = CSharpCompositeTypeDeclaration.findCompositeType((CSharpTypeDeclaration) listOwner);
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
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
