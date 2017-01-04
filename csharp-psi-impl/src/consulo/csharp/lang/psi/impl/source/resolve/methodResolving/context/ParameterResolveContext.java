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
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Trinity;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public interface ParameterResolveContext<T>
{
	@Nullable
	T getParameterByIndex(int i);

	@Nullable
	T getParameterByName(@NotNull String name);

	int getParametersSize();

	@Nullable
	DotNetParameter getParamsParameter();

	@NotNull
	T[] getParameters();

	@NotNull
	DotNetTypeRef getParamsParameterTypeRef();

	@NotNull
	DotNetTypeRef getInnerParamsParameterTypeRef();

	boolean isResolveFromParentTypeRef();

	/**
	 * Return parameter info
	 * 1. Name
	 * 2. TypeRef
	 * 3. Optional flag
	 */
	@NotNull
	@RequiredReadAction
	Trinity<String, DotNetTypeRef, Boolean> getParameterInfo(@NotNull T parameter);
}
