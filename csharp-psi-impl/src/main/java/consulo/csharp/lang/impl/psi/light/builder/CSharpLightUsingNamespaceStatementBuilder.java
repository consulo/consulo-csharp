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

package consulo.csharp.lang.impl.psi.light.builder;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 19.03.2016
 */
public class CSharpLightUsingNamespaceStatementBuilder extends LightElement implements CSharpUsingNamespaceStatement
{
	private DotNetNamespaceAsElement myElement;
	private GlobalSearchScope myResolveScope;

	public CSharpLightUsingNamespaceStatementBuilder(@Nonnull DotNetNamespaceAsElement element, GlobalSearchScope resolveScope)
	{
		super(element.getManager(), CSharpLanguage.INSTANCE);
		myElement = element;
		myResolveScope = resolveScope;
	}

	@Nonnull
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
	@Nonnull
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
