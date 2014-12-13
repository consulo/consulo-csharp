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
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayType;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpNullableType;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CS0305 extends CompilerCheck<DotNetType>
{
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull DotNetType type)
	{
		int foundCount = 0;
		PsiElement elementForHighlight = type;
		if(type instanceof CSharpUserType)
		{
			elementForHighlight = ((CSharpUserType) type).getReferenceExpression();
			foundCount = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(((CSharpUserType) type).getReferenceExpression());
		}
		else if(type instanceof CSharpArrayType)
		{
			elementForHighlight = ((CSharpArrayType) type).getInnerType();
			foundCount = 1;
		}
		else if(type instanceof CSharpNullableType)
		{
			elementForHighlight = ((CSharpNullableType) type).getInnerType();
			foundCount = 1;
		}

		DotNetTypeResolveResult typeResolveResult = type.toTypeRef().resolve(type);

		int expectedCount = 0;
		PsiElement resolvedElement = typeResolveResult.getElement();
		if(resolvedElement == null)
		{
			return null;
		}

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
			return newBuilder(elementForHighlight, formatElement(resolvedElement), String.valueOf(expectedCount));
		}
		return null;
	}
}
