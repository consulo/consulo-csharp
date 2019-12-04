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

package consulo.csharp.lang.psi.impl.resolve;

import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.util.dataholder.UserDataHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class CSharpUsingNamespaceOrTypeResolveContext implements CSharpResolveContext
{
	private CSharpUsingListChild myUsingListChild;

	private NotNullLazyValue<CSharpResolveContext> myContextValue = new NotNullLazyValue<CSharpResolveContext>()
	{
		@Nonnull
		@Override
		@RequiredReadAction
		protected CSharpResolveContext compute()
		{
			PsiElement targetElement = null;
			DotNetGenericExtractor extractor = DotNetGenericExtractor.EMPTY;

			if(myUsingListChild instanceof CSharpUsingNamespaceStatement)
			{
				targetElement = ((CSharpUsingNamespaceStatement) myUsingListChild).resolve();
			}
			else if(myUsingListChild instanceof CSharpUsingTypeStatement)
			{
				DotNetTypeRef typeRef = ((CSharpUsingTypeStatement) myUsingListChild).getTypeRef();

				DotNetTypeResolveResult typeResolveResult = typeRef.resolve();
				targetElement = typeResolveResult.getElement();
				extractor = typeResolveResult.getGenericExtractor();
			}
			if(targetElement == null)
			{
				return CSharpResolveContext.EMPTY;
			}
			return CSharpResolveContextUtil.createContext(extractor, myUsingListChild.getResolveScope(), targetElement);
		}
	};

	public CSharpUsingNamespaceOrTypeResolveContext(@Nonnull CSharpUsingListChild usingListChild)
	{
		myUsingListChild = usingListChild;
	}

	@Nonnull
	@Override
	public PsiElement getElement()
	{
		return myUsingListChild;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@Nonnull String name)
	{
		return myContextValue.getValue().findExtensionMethodGroupByName(name);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@Nonnull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		return myContextValue.getValue().processExtensionMethodGroups(processor);
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@Nonnull Processor<PsiElement> processor, boolean deep)
	{
		return myContextValue.getValue().processElements(processor, deep);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Collection<PsiElement> findByName(@Nonnull String name, boolean deep, @Nonnull UserDataHolder holder)
	{
		return myContextValue.getValue().findByName(name, deep, holder);
	}
}
