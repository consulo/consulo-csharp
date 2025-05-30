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

package consulo.csharp.impl.ide.highlight.check.impl;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpElements;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;

/**
 * @author VISTALL
 * @since 08.01.2016
 */
public class CS0509 extends CompilerCheck<DotNetType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetType element)
	{
		PsiElement parent = element.getParent();
		if(parent instanceof DotNetTypeList && PsiUtilCore.getElementType(parent) == CSharpElements.EXTENDS_LIST)
		{
			PsiElement superParent = parent.getParent();
			if(superParent instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) superParent).isEnum())
			{
				return null;
			}
			PsiElement psiElement = element.toTypeRef().resolve().getElement();
			if(psiElement instanceof CSharpTypeDeclaration)
			{
				if(((CSharpTypeDeclaration) psiElement).hasModifier(DotNetModifier.SEALED))
				{
					return newBuilder(element, formatElement(parent.getParent()), ((CSharpTypeDeclaration) psiElement).getVmQName());
				}
			}
		}
		return null;
	}
}
