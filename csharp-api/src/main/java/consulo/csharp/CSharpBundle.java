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

package consulo.csharp;

import consulo.annotation.DeprecationInfo;
import consulo.component.util.localize.AbstractBundle;
import org.jetbrains.annotations.PropertyKey;

/**
 * @author VISTALL
 * @since 15.05.14
 */
@Deprecated
@DeprecationInfo("Use CSharpLocalize")
public class CSharpBundle extends AbstractBundle
{
	private static final CSharpBundle ourInstance = new CSharpBundle();

	private CSharpBundle()
	{
		super("messages.CSharpBundle");
	}

	public static String message(@PropertyKey(resourceBundle = "messages.CSharpBundle") String key)
	{
		return ourInstance.getMessage(key);
	}

	public static String message(@PropertyKey(resourceBundle = "messages.CSharpBundle") String key, Object... params)
	{
		return ourInstance.getMessage(key, params);
	}
}
