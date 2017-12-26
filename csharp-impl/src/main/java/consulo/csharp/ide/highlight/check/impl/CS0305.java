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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CS0305 extends CompilerCheck<CSharpReferenceExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpReferenceExpression referenceExpression)
	{
		if(referenceExpression.getParent() instanceof CSharpMethodCallExpressionImpl)
		{
			return null;
		}

		CSharpReferenceExpression.ResolveToKind kind = referenceExpression.kind();
		switch(kind)
		{
			case THIS:
			case BASE:
			case THIS_CONSTRUCTOR:
			case BASE_CONSTRUCTOR:
				return null;
		}

		PsiElement resolvedElement = referenceExpression.resolve();
		if(resolvedElement == null)
		{
			return null;
		}

		int expectedCount = 0;
		int foundCount = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(referenceExpression);

		if(resolvedElement instanceof CSharpConstructorDeclaration)
		{
			resolvedElement = resolvedElement.getParent();
		}

		if(resolvedElement instanceof DotNetGenericParameterListOwner)
		{
			expectedCount = ((DotNetGenericParameterListOwner) resolvedElement).getGenericParametersCount();
		}

		if(expectedCount != foundCount)
		{
			return newBuilder(referenceExpression.getReferenceElement(), formatElement(resolvedElement), String.valueOf(expectedCount));
		}
		return null;
	}
}
