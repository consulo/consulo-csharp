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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class MethodParameterResolveContext implements ParameterResolveContext<DotNetParameter>
{
	private final PsiElement myScope;
	private final boolean myResolveFromParent;
	private final DotNetParameter[] myParameters;
	private DotNetParameter myParamsParameter;

	private final NotNullLazyValue<DotNetTypeRef> myInnerParamsParameterTypeRefValue;
	private final NotNullLazyValue<DotNetTypeRef> myParamsParameterTypeRefValue;

	public MethodParameterResolveContext(DotNetParameterListOwner parameterListOwner, PsiElement scope, boolean resolveFromParent)
	{
		myScope = scope;
		myResolveFromParent = resolveFromParent;
		myParameters = parameterListOwner.getParameters();
		myParamsParameter = ArrayUtil.getLastElement(myParameters);
		if(myParamsParameter != null && !myParamsParameter.hasModifier(CSharpModifier.PARAMS))
		{
			myParamsParameter = null;
		}

		myInnerParamsParameterTypeRefValue = NotNullLazyValue.createValue(() -> myParamsParameter == null ? DotNetTypeRef.ERROR_TYPE : CSharpResolveUtil.resolveIterableType(myScope,
				getParamsParameterTypeRef()));
		myParamsParameterTypeRefValue = NotNullLazyValue.createValue(() -> myParamsParameter == null ? DotNetTypeRef.ERROR_TYPE : myParamsParameter.toTypeRef(true));
	}

	@Override
	@NotNull
	public DotNetTypeRef getInnerParamsParameterTypeRef()
	{
		return myInnerParamsParameterTypeRefValue.getValue();
	}

	@Override
	@NotNull
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
	public DotNetParameter getParameterByName(@NotNull String name)
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

	@NotNull
	@RequiredReadAction
	@Override
	public Trinity<String, DotNetTypeRef, Boolean> getParameterInfo(@NotNull DotNetParameter parameter)
	{
		return Trinity.create(parameter.getName(), parameter.toTypeRef(true), parameter.hasModifier(CSharpModifier.OPTIONAL));
	}

	@NotNull
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