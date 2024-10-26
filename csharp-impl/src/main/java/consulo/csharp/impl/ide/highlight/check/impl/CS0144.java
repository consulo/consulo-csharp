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

import jakarta.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetType;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05.01.15
 */
public class CS0144 extends CompilerCheck<CSharpNewExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpNewExpression element)
	{
		PsiElement resolvedNewElement = element.toTypeRef(false).resolve().getElement();
		if(resolvedNewElement instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) resolvedNewElement).hasModifier(DotNetModifier.ABSTRACT))
		{
			DotNetType newType = element.getNewType();
			assert newType != null;
			return newBuilder(newType, formatElement(resolvedNewElement));
		}
		return null;
	}
}
