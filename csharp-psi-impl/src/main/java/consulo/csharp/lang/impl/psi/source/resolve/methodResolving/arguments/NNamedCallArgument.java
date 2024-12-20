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

import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NNamedCallArgument extends NCallArgument
{
	private String myName;

	public NNamedCallArgument(@Nonnull DotNetTypeRef typeRef, @Nullable CSharpCallArgument callArgument, @Nullable Object parameter, @Nullable String name)
	{
		super(typeRef, callArgument, parameter);
		myName = name;
	}

	@Nullable
	public String getName()
	{
		return myName;
	}
}
