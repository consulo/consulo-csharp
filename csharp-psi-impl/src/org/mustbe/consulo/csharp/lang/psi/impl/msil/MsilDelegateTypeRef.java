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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.dotnet.msil.decompiler.util.MsilHelper;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilDelegateTypeRef extends DotNetTypeRef.Delegate
{
	@NotNull
	private final PsiElement myScope;

	public MsilDelegateTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef typeRef)
	{
		super(typeRef);
		myScope = scope;
	}

	@NotNull
	@Override
	public String getPresentableText()
	{
		return MsilHelper.cutGenericMarker(super.getPresentableText());
	}

	@NotNull
	@Override
	public String getQualifiedText()
	{
		return MsilHelper.prepareForUser(super.getQualifiedText());
	}

	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull final PsiElement scope)
	{
		return resolveImpl();
	}

	@NotNull
	@LazyInstance
	public DotNetTypeResolveResult resolveImpl()
	{
		return new DotNetTypeResolveResult()
		{
			private DotNetTypeResolveResult cachedResult = MsilDelegateTypeRef.this.getDelegate().resolve(myScope);

			@Nullable
			@Override
			public PsiElement getElement()
			{
				PsiElement element = cachedResult.getElement();
				if(element == null)
				{
					return null;
				}
				return MsilToCSharpUtil.wrap(cachedResult.getElement());
			}

			@NotNull
			@Override
			public DotNetGenericExtractor getGenericExtractor()
			{
				return cachedResult.getGenericExtractor();
			}

			@Override
			public boolean isNullable()
			{
				return CSharpTypeUtil.isElementIsNullable(getElement());
			}
		};
	}
}
