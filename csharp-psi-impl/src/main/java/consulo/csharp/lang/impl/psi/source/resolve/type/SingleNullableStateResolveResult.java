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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.NotNullLazyValue;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 24.05.2015
 */
public abstract class SingleNullableStateResolveResult implements DotNetTypeResolveResult
{
	private NotNullLazyValue<Boolean> myNullalbeCacheValue = NotNullLazyValue.createValue(this::isNullableImpl);

	@RequiredReadAction
	public abstract boolean isNullableImpl();

	@Override
	public final boolean isNullable()
	{
		return myNullalbeCacheValue.getValue();
	}
}
