/*
 * Copyright 2013-2014 must-be.org
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
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CS0305 extends CompilerCheck<CSharpReferenceExpression>
{
	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpReferenceExpression referenceExpression)
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
