/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericExtractor;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.*;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-07-27
 */
public class CSharpPossibleNullableTypeRef extends DotNetTypeRefWithCachedResult
{
	private final DotNetTypeRef myInnerTypeRef;

	public CSharpPossibleNullableTypeRef(DotNetTypeRef innerTypeRef)
	{
		super(innerTypeRef.getProject(), innerTypeRef.getResolveScope());
		myInnerTypeRef = innerTypeRef;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeResolveResult result = myInnerTypeRef.resolve();

		PsiElement element = result.getElement();

		if(element == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}

		if(CSharpTypeUtil.isNullableElement(element))
		{
			return new SimpleTypeResolveResult(element, true);
		}
		else
		{
			DotNetTypeDeclaration nullableType = DotNetPsiSearcher.getInstance(element.getProject()).findType(DotNetTypes.System.Nullable$1, getResolveScope(), CSharpTransform.INSTANCE);
			if(nullableType == null)
			{
				return result;
			}
			DotNetGenericExtractor extractor = CSharpGenericExtractor.create(nullableType.getGenericParameters(), new DotNetTypeRef[]{myInnerTypeRef});
			return new SimpleTypeResolveResult(nullableType, extractor, true);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
	{
		return myInnerTypeRef.getVmQName() + "?";
	}
}
