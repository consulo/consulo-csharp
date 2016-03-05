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

package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import org.mustbe.consulo.csharp.lang.psi.resolve.BaseCSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class CSharpUsingNamespaceOrTypeResolveContext extends BaseCSharpResolveContext
{
	private CSharpUsingListChild myUsingListChild;

	private NotNullLazyValue<CSharpResolveContext> myContextValue = new NotNullLazyValue<CSharpResolveContext>()
	{
		@NotNull
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

				DotNetTypeResolveResult typeResolveResult = typeRef.resolve(myUsingListChild);
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

	public CSharpUsingNamespaceOrTypeResolveContext(@NotNull CSharpUsingListChild usingListChild)
	{
		myUsingListChild = usingListChild;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		return myContextValue.getValue().findExtensionMethodGroupByName(name);
	}

	@RequiredReadAction
	@Override
	public boolean processExtensionMethodGroups(@NotNull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		return myContextValue.getValue().processExtensionMethodGroups(processor);
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		return myContextValue.getValue().processElements(processor, deep);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		return myContextValue.getValue().findByName(name, deep, holder);
	}
}
