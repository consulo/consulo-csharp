/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator;

import org.jetbrains.annotations.Nullable;
import mono.debugger.ObjectValueMirror;
import mono.debugger.StringValueMirror;
import mono.debugger.Value;

/**
 * @author VISTALL
 * @since 09.03.2016
 */
public class ObjectValueMirrorUtil
{
	@Nullable
	public static ObjectValueMirror extractObjectValueMirror(@Nullable Value<?> value)
	{
		if(value instanceof ObjectValueMirror)
		{
			return (ObjectValueMirror) value;
		}

		if(value instanceof StringValueMirror)
		{
			return ((StringValueMirror) value).object();
		}
		return null;
	}
}
