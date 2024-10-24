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
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetElement;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 24.05.2015
 */
public class CS0023 extends CompilerCheck<DotNetElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull DotNetElement element)
	{
		if(element instanceof CSharpReferenceExpression)
		{
			PsiElement qualifier = ((CSharpReferenceExpression) element).getQualifier();
			if(isNullConstant(qualifier))
			{
				PsiElement memberAccessElement = ((CSharpReferenceExpression) element).getMemberAccessElement();
				assert memberAccessElement != null;
				return newBuilder(memberAccessElement, ".", "null");
			}
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}

	public static boolean isNullConstant(@Nullable PsiElement element)
	{
		return element instanceof CSharpConstantExpressionImpl && ((CSharpConstantExpressionImpl) element).getLiteralType() == CSharpTokens
				.NULL_LITERAL;
	}
}
