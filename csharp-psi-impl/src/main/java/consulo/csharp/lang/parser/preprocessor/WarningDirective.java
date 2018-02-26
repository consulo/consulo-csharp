/*
 * Copyright 2013-2018 consulo.io
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

package consulo.csharp.lang.parser.preprocessor;

/**
 * @author VISTALL
 * @since 2018-02-26
 */
public class WarningDirective extends PreprocessorDirective
{
	private String myText;

	public WarningDirective(String text)
	{
		myText = text;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("WarningDirective{");
		sb.append("myText='").append(myText).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
