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

package consulo.csharp.impl.ide.debugger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxyVisitor;

/**
 * @author VISTALL
 * @since 02.05.2016
 */
public class CSharpStaticValueProxy implements DotNetValueProxy
{
	public static final CSharpStaticValueProxy INSTANCE = new CSharpStaticValueProxy();

	@Nullable
	@Override
	public DotNetTypeProxy getType()
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Object getValue()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void accept(DotNetValueProxyVisitor visitor)
	{
	}
}
