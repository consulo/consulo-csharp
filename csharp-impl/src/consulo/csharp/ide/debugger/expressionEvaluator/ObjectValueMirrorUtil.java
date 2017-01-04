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

package consulo.csharp.ide.debugger.expressionEvaluator;

import org.jetbrains.annotations.Nullable;
import consulo.dotnet.debugger.proxy.value.DotNetObjectValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetStringValueProxy;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class ObjectValueMirrorUtil
{
	@Nullable
	public static DotNetObjectValueProxy extractObjectValueMirror(@Nullable DotNetValueProxy value)
	{
		if(value instanceof DotNetObjectValueProxy)
		{
			return (DotNetObjectValueProxy) value;
		}

		if(value instanceof DotNetStringValueProxy)
		{
			return (DotNetObjectValueProxy) ((DotNetStringValueProxy) value).getObjectValue();
		}
		return null;
	}
}
