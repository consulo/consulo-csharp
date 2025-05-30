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
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.util.lang.Comparing;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CS0693 extends CompilerCheck<DotNetGenericParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetGenericParameter element)
	{
		PsiElement nameIdentifier = element.getNameIdentifier();
		if(nameIdentifier == null)
		{
			return null;
		}

		DotNetGenericParameterListOwner firstParent = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);

		DotNetGenericParameterListOwner secondParent = PsiTreeUtil.getParentOfType(firstParent, DotNetGenericParameterListOwner.class);

		if(secondParent != null)
		{
			for(DotNetGenericParameter genericParameter : secondParent.getGenericParameters())
			{
				if(Comparing.equal(genericParameter.getName(), nameIdentifier.getText()))
				{
					return newBuilder(nameIdentifier, formatElement(element), formatElement(secondParent));
				}
			}
		}
		return null;
	}
}
