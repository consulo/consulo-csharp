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

package consulo.csharp.ide;

import org.jetbrains.annotations.PropertyKey;
import com.intellij.AbstractBundle;

/**
 * @author VISTALL
 * @since 17.01.14
 */
public class CSharpErrorBundle extends AbstractBundle
{
	private static final CSharpErrorBundle ourInstance = new CSharpErrorBundle();

	private CSharpErrorBundle()
	{
		super("messages.CSharpErrorBundle");
	}

	public static String message(@PropertyKey(resourceBundle = "messages.CSharpErrorBundle") String key)
	{
		return ourInstance.getMessage(key);
	}

	public static String message(@PropertyKey(resourceBundle = "messages.CSharpErrorBundle") String key, Object... params)
	{
		return ourInstance.getMessage(key, params);
	}
}
