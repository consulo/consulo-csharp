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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpTypeRefByTypeDeclaration extends DotNetTypeRef.Adapter
{
	private DotNetTypeDeclaration myElement;
	@NotNull
	private final DotNetGenericExtractor myExtractor;

	public CSharpTypeRefByTypeDeclaration(@NotNull DotNetTypeDeclaration element)
	{
		this(element, DotNetGenericExtractor.EMPTY);
	}

	public CSharpTypeRefByTypeDeclaration(@NotNull DotNetTypeDeclaration element, @NotNull DotNetGenericExtractor extractor)
	{
		myElement = element;
		myExtractor = extractor;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return myElement.getName();
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return myElement.getPresentableQName();
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return new SimpleTypeResolveResult(myElement, myExtractor, CSharpTypeUtil.isElementIsNullable(myElement));
	}
}
