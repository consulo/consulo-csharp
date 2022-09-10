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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.lang.Comparing;

/**
 * @author VISTALL
 * @since 01.11.2015
 */
public class CS0692 extends CompilerCheck<DotNetGenericParameter>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetGenericParameter element)
	{
		DotNetGenericParameterListOwner listOwner = PsiTreeUtil.getParentOfType(element, DotNetGenericParameterListOwner.class);
		if(listOwner != null)
		{
			for(DotNetGenericParameter parameter : listOwner.getGenericParameters())
			{
				if(parameter == element)
				{
					continue;
				}
				if(Comparing.equal(parameter.getName(), element.getName()))
				{
					if(element.getIndex() > parameter.getIndex())
					{
						PsiElement nameIdentifier = element.getNameIdentifier();
						assert nameIdentifier != null;
						return newBuilder(nameIdentifier, formatElement(element));
					}
				}
			}
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
