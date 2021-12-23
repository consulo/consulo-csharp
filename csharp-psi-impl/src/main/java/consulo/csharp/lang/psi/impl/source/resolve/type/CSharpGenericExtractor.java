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

import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class CSharpGenericExtractor implements DotNetGenericExtractor
{
	@Nonnull
	public static DotNetGenericExtractor create(@Nonnull Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		if(map.isEmpty())
		{
			return EMPTY;
		}
		return new CSharpGenericExtractor(map);
	}

	@Nonnull
	public static DotNetGenericExtractor create(@Nonnull DotNetGenericParameter[] genericParameters, @Nonnull DotNetTypeRef[] arguments)
	{
		if(genericParameters.length == 0 || genericParameters.length != arguments.length)
		{
			return EMPTY;
		}
		return new CSharpGenericExtractor(genericParameters, arguments);
	}

	private DotNetGenericParameter[] myGenericParameters;
	private DotNetTypeRef[] myTypeRefs;

	private CSharpGenericExtractor(Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		this(ContainerUtil.toArray(map.keySet(), DotNetGenericParameter.ARRAY_FACTORY), ContainerUtil.toArray(map.values(), DotNetTypeRef.ARRAY_FACTORY));
	}

	private CSharpGenericExtractor(DotNetGenericParameter[] genericParameters, DotNetTypeRef[] arguments)
	{
		myGenericParameters = genericParameters;
		myTypeRefs = arguments;
		assert myGenericParameters.length != 0 : "can't be empty";
	}

	@Nullable
	@Override
	public DotNetTypeRef extract(@Nonnull DotNetGenericParameter parameter)
	{
		int index = -1;
		for(int i = 0; i < myGenericParameters.length; i++)
		{
			DotNetGenericParameter genericParameter = myGenericParameters[i];
			if(genericParameter.isEquivalentTo(parameter))
			{
				index = i;
				break;
			}
		}

		if(index == -1)
		{
			return null;
		}

		return ArrayUtil2.safeGet(myTypeRefs, index);
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		CSharpGenericExtractor that = (CSharpGenericExtractor) o;
		return isEquivalentTo(myGenericParameters, that.myGenericParameters) && Arrays.equals(myTypeRefs, that.myTypeRefs);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(myGenericParameters, myTypeRefs);
	}

	private static boolean isEquivalentTo(PsiElement[] elements1, PsiElement[] elements2)
	{
		if(elements1.length != elements2.length)
		{
			return false;
		}

		for(int i = 0; i < elements1.length; i++)
		{
			PsiElement element1 = elements1[i];
			PsiElement element2 = elements2[i];
			if(!element1.isEquivalentTo(element2))
			{
				return false;
			}
		}
		return true;
	}
}
