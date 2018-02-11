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

package consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Trinity;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public class SimpleParameterResolveContext implements ParameterResolveContext<CSharpSimpleParameterInfo>
{
	@Nonnull
	private final CSharpSimpleParameterInfo[] myParameterInfos;

	public SimpleParameterResolveContext(@Nonnull CSharpSimpleParameterInfo[] parameterInfos)
	{
		myParameterInfos = parameterInfos;
	}

	@Nullable
	@Override
	public CSharpSimpleParameterInfo getParameterByIndex(int i)
	{
		return ArrayUtil2.safeGet(myParameterInfos, i);
	}

	@Nullable
	@Override
	public CSharpSimpleParameterInfo getParameterByName(@Nonnull String name)
	{
		for(CSharpSimpleParameterInfo parameterInfo : myParameterInfos)
		{
			if(Comparing.equal(parameterInfo.getNotNullName(), name))
			{
				return parameterInfo;
			}
		}
		return null;
	}

	@Override
	public int getParametersSize()
	{
		return myParameterInfos.length;
	}

	@Nullable
	@Override
	public DotNetParameter getParamsParameter()
	{
		return null;
	}

	@Nonnull
	@Override
	public CSharpSimpleParameterInfo[] getParameters()
	{
		return myParameterInfos;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getParamsParameterTypeRef()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getInnerParamsParameterTypeRef()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	public boolean isResolveFromParentTypeRef()
	{
		return false;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Trinity<String, DotNetTypeRef, Boolean> getParameterInfo(@Nonnull CSharpSimpleParameterInfo parameter)
	{
		return Trinity.create(parameter.getNotNullName(), parameter.getTypeRef(), parameter.isOptional());
	}
}
