/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 19.03.2016
 */
public class CSharpLightUsingNamespaceStatementBuilder extends LightElement implements CSharpUsingNamespaceStatement
{
	private DotNetNamespaceAsElement myElement;
	private GlobalSearchScope myResolveScope;

	public CSharpLightUsingNamespaceStatementBuilder(@NotNull DotNetNamespaceAsElement element, GlobalSearchScope resolveScope)
	{
		super(element.getManager(), CSharpLanguage.INSTANCE);
		myElement = element;
		myResolveScope = resolveScope;
	}

	@NotNull
	@Override
	public GlobalSearchScope getResolveScope()
	{
		return myResolveScope;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getReferenceText()
	{
		return myElement.getPresentableQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetNamespaceAsElement resolve()
	{
		return myElement;
	}

	@Nullable
	@Override
	public DotNetReferenceExpression getNamespaceReference()
	{
		return null;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement getUsingKeywordElement()
	{
		throw new IllegalArgumentException();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getReferenceElement()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return "CSharpLightUsingNamespaceStatementBuilder: " + getReferenceText();
	}
}
