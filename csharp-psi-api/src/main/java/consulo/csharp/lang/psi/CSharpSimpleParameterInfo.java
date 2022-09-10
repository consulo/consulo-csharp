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

package consulo.csharp.lang.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ArrayFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public class CSharpSimpleParameterInfo
{
	public static final CSharpSimpleParameterInfo[] EMPTY_ARRAY = new CSharpSimpleParameterInfo[0];

	public static ArrayFactory<CSharpSimpleParameterInfo> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpSimpleParameterInfo[count];

	@Nonnull
	public static DotNetTypeRef[] toTypeRefs(@Nonnull CSharpSimpleParameterInfo[] parameterInfos)
	{
		if(parameterInfos.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameterInfos.length];
		for(int i = 0; i < parameterInfos.length; i++)
		{
			CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
			typeRefs[i] = parameterInfo.getTypeRef();
		}
		return typeRefs;
	}

	private int myIndex;
	private String myName;
	@Nullable
	private final PsiElement myElement;
	private DotNetTypeRef myTypeRef;

	private boolean myOptional;

	@RequiredReadAction
	public CSharpSimpleParameterInfo(int index, @Nonnull DotNetParameter parameter, @Nonnull DotNetTypeRef typeRef)
	{
		myIndex = index;
		myName = parameter.getName();
		myElement = parameter;
		myTypeRef = typeRef;
		myOptional = parameter.hasModifier(CSharpModifier.OPTIONAL);
	}

	@RequiredReadAction
	public CSharpSimpleParameterInfo(int index, @Nullable String name, @Nullable PsiElement element, @Nonnull DotNetTypeRef typeRef)
	{
		myIndex = index;
		myName = name;
		myElement = element;
		myTypeRef = typeRef;
	}

	public boolean isOptional()
	{
		return myOptional;
	}

	@Nullable
	public PsiElement getElement()
	{
		return myElement;
	}

	public int getIndex()
	{
		return myIndex;
	}

	@Nullable
	public String getName()
	{
		return myName;
	}

	@Nonnull
	public String getNotNullName()
	{
		return myName == null ? "p" + myIndex : myName;
	}

	@Nonnull
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}
}
