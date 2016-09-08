/*
 * Copyright 2013-2016 must-be.org
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
import org.mustbe.consulo.csharp.lang.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;

/**
 * @author VISTALL
 * @since 08.01.2016
 */
public class CS0709 extends CompilerCheck<DotNetType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull DotNetType element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof DotNetTypeList && PsiUtilCore.getElementType(parent) == CSharpElements.EXTENDS_LIST)
		{
			PsiElement psiElement = element.toTypeRef().resolve().getElement();
			if(psiElement instanceof CSharpTypeDeclaration)
			{
				if(((CSharpTypeDeclaration) psiElement).hasModifier(DotNetModifier.STATIC))
				{
					return newBuilder(element, formatElement(parent.getParent()), ((CSharpTypeDeclaration) psiElement).getVmQName());
				}
			}
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
