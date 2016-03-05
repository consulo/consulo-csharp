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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.resolve.BaseCSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class CSharpTypeDefResolveContext extends BaseCSharpResolveContext
{
	private CSharpTypeDefStatement myStatement;

	private NotNullLazyValue<CSharpResolveContext> myValue = new NotNullLazyValue<CSharpResolveContext>()
	{
		@NotNull
		@Override
		@RequiredReadAction
		protected CSharpResolveContext compute()
		{
			DotNetTypeResolveResult typeResolveResult = myStatement.toTypeRef().resolve(myStatement);

			PsiElement element = typeResolveResult.getElement();
			if(element == null)
			{
				return EMPTY;
			}
			return CSharpResolveContextUtil.createContext(typeResolveResult.getGenericExtractor(), myStatement.getResolveScope(), element);
		}
	};

	private String myName;

	public CSharpTypeDefResolveContext(CSharpTypeDefStatement statement)
	{
		myStatement = statement;
		myName = myStatement.getName();
	}

	@RequiredReadAction
	@Override
	public boolean processElements(@NotNull Processor<PsiElement> processor, boolean deep)
	{
		return processor.process(myStatement);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public PsiElement[] findByName(@NotNull String name, boolean deep, @NotNull UserDataHolder holder)
	{
		return name.equals(myName) ? new PsiElement[] {myStatement} : PsiElement.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public PsiElement getElement()
	{
		return myStatement;
	}
}
