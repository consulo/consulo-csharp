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

package consulo.csharp.lang.impl.psi.source.resolve.sorter;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;

import javax.annotation.Nonnull;
import java.util.Comparator;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public class TypeLikeComparator implements Comparator<ResolveResult>
{
	@Nonnull
	@RequiredReadAction
	public static TypeLikeComparator create(@Nonnull PsiElement element)
	{
		return new TypeLikeComparator(CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(element));
	}

	private final int myGenericCount;

	public TypeLikeComparator(int genericCount)
	{
		myGenericCount = genericCount;
	}

	@Override
	public int compare(ResolveResult o1, ResolveResult o2)
	{
		return getWeight(o2) - getWeight(o1);
	}

	public int getWeight(ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();

		if(element instanceof DotNetVariable)
		{
			return 200000;
		}

		if(element instanceof CSharpElementGroup)
		{
			return 100000;
		}

		if(element instanceof CSharpTypeDefStatement)
		{
			return 50100;
		}

		if(element instanceof DotNetGenericParameterListOwner)
		{
			if(((DotNetGenericParameterListOwner) element).getGenericParametersCount() == myGenericCount)
			{
				return 50000;
			}
			return -((DotNetGenericParameterListOwner) element).getGenericParametersCount() * 100;
		}

		if(element instanceof DotNetNamespaceAsElement)
		{
			return 0;
		}

		return 10;
	}
}
