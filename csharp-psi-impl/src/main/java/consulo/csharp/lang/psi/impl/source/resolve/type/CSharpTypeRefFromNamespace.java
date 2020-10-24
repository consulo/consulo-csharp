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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import com.intellij.psi.search.GlobalSearchScope;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.resolve.SimpleTypeResolveResult;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpTypeRefFromNamespace extends DotNetTypeRefWithCachedResult
{
	private DotNetNamespaceAsElement myNamespaceAsElement;

	public CSharpTypeRefFromNamespace(DotNetNamespaceAsElement namespaceAsElement, GlobalSearchScope resolveScope)
	{
		super(namespaceAsElement.getProject(), resolveScope);
		myNamespaceAsElement = namespaceAsElement;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new SimpleTypeResolveResult(myNamespaceAsElement);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return myNamespaceAsElement.getPresentableQName();
	}
}
