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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpUserType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public class CS0146 extends CompilerCheck<CSharpUserType>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpUserType element)
	{
		if(element.getParent() instanceof DotNetTypeList && element.getParent().getParent() instanceof CSharpTypeDeclaration)
		{
			CSharpTypeDeclaration parent = (CSharpTypeDeclaration) element.getParent().getParent();

			DotNetTypeResolveResult typeResolveResult = element.toTypeRef().resolve();

			PsiElement resolvedElement = typeResolveResult.getElement();
			if(resolvedElement instanceof CSharpTypeDeclaration)
			{
				CSharpTypeDeclaration resolvedTypeDeclaration = (CSharpTypeDeclaration) resolvedElement;
				if(resolvedElement == parent || resolvedTypeDeclaration.isInheritor(((CSharpTypeDeclaration) resolvedElement).getVmQName(), true))
				{
					return newBuilder(element, formatElement(parent), formatElement(resolvedElement));
				}
			}
		}
		return null;
	}
}
