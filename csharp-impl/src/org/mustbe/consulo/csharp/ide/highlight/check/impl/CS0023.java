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
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 24.05.2015
 */
public class CS0023 extends CompilerCheck<DotNetElement>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetElement element)
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
		return super.checkImpl(languageVersion, element);
	}

	public static boolean isNullConstant(@Nullable PsiElement element)
	{
		return element instanceof CSharpConstantExpressionImpl && ((CSharpConstantExpressionImpl) element).getLiteralType() == CSharpTokens
				.NULL_LITERAL;
	}
}
