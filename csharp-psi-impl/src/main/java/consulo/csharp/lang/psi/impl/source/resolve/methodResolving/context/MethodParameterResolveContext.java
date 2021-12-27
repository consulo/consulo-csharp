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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodParameterResolveContext implements ParameterResolveContext<DotNetParameter>
{
	private final DotNetParameterListOwner myParameterListOwner;
	private final boolean myResolveFromParent;
	private final DotNetParameter[] myParameters;
	private DotNetParameter myParamsParameter;

	private final NotNullLazyValue<DotNetTypeRef> myInnerParamsParameterTypeRefValue;
	private final NotNullLazyValue<DotNetTypeRef> myParamsParameterTypeRefValue;

	@RequiredReadAction
	public MethodParameterResolveContext(DotNetParameterListOwner parameterListOwner, boolean resolveFromParent)
	{
		myParameterListOwner = parameterListOwner;
		myResolveFromParent = resolveFromParent;
		myParameters = parameterListOwner.getParameters();
		myParamsParameter = ArrayUtil.getLastElement(myParameters);
		if(myParamsParameter != null && !myParamsParameter.hasModifier(CSharpModifier.PARAMS))
		{
			myParamsParameter = null;
		}

		myInnerParamsParameterTypeRefValue = NotNullLazyValue.createValue(() -> myParamsParameter == null ? DotNetTypeRef.ERROR_TYPE : CSharpResolveUtil.resolveIterableType(getParamsParameterTypeRef
				()));
		myParamsParameterTypeRefValue = NotNullLazyValue.createValue(() -> myParamsParameter == null ? DotNetTypeRef.ERROR_TYPE : myParamsParameter.toTypeRef(true));
	}

	@Nonnull
	@Override
	public Project getProject()
	{
		return myParameterListOwner.getProject();
	}

	@Nonnull
	@Override
	public GlobalSearchScope getResolveScope()
	{
		return myParameterListOwner.getResolveScope();
	}

	@Override
	@Nonnull
	public DotNetTypeRef getInnerParamsParameterTypeRef()
	{
		return myInnerParamsParameterTypeRefValue.getValue();
	}

	@Override
	@Nonnull
	public DotNetTypeRef getParamsParameterTypeRef()
	{
		return myParamsParameterTypeRefValue.getValue();
	}

	@Override
	@Nullable
	public DotNetParameter getParamsParameter()
	{
		return myParamsParameter;
	}

	@Override
	public int getParametersSize()
	{
		return myParameters.length;
	}

	@Override
	@Nullable
	public DotNetParameter getParameterByIndex(int i)
	{
		return ArrayUtil2.safeGet(myParameters, i);
	}

	@Override
	public DotNetParameter getParameterByName(@Nonnull String name)
	{
		for(DotNetParameter parameter : myParameters)
		{
			if(Comparing.equal(parameter.getName(), name))
			{
				return parameter;
			}
		}
		return null;
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public Trinity<String, DotNetTypeRef, Boolean> getParameterInfo(@Nonnull DotNetParameter parameter)
	{
		return Trinity.create(parameter.getName(), parameter.toTypeRef(true), parameter.hasModifier(CSharpModifier.OPTIONAL));
	}

	@Nonnull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameters;
	}

	@Override
	public boolean isResolveFromParentTypeRef()
	{
		return myResolveFromParent;
	}
}
