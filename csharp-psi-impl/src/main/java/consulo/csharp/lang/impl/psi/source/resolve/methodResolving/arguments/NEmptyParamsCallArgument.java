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

package consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.scope.GlobalSearchScope;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 12.11.14
 */
public class NEmptyParamsCallArgument extends NParamsCallArgument
{
	@RequiredReadAction
	public NEmptyParamsCallArgument(@Nonnull DotNetParameter parameter)
	{
		super(parameter.getProject(), parameter.getResolveScope(), List.of(), parameter);
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@RequiredReadAction
	@Override
	public int calcValid(@Nonnull GlobalSearchScope implicitCastType)
	{
		return PARAMS;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRef()
	{
		DotNetTypeRef parameterTypeRef = getParameterTypeRef();
		assert parameterTypeRef != null;
		return parameterTypeRef;
	}
}
