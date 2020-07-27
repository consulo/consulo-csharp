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

package consulo.csharp.lang.psi.impl;

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.*;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2020-07-27
 */
public class CSharpPossibleNullableTypeRef extends DotNetTypeRefWithCachedResult
{
	private final PsiElement myScope;
	private final DotNetTypeRef myInnerTypeRef;

	public CSharpPossibleNullableTypeRef(PsiElement scope, DotNetTypeRef innerTypeRef)
	{
		super(scope.getProject());
		myScope = scope;
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
			DotNetTypeDeclaration nullableType = DotNetPsiSearcher.getInstance(element.getProject()).findType(DotNetTypes.System.Nullable$1, myScope.getResolveScope(), CSharpTransform.INSTANCE);
			if(nullableType == null)
			{
				return result;
			}
			DotNetGenericExtractor extractor = CSharpGenericExtractor.create(nullableType.getGenericParameters(), new DotNetTypeRef[]{myInnerTypeRef});
			return new SimpleTypeResolveResult(nullableType, extractor);
		}
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return myInnerTypeRef.toString() + "?";
	}
}
