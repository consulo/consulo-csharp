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

package consulo.csharp.lang.impl.parser.preprocessor;

/**
 * @author VISTALL
 * @since 02.03.2016
 */
public class RegionPreprocessorDirective extends PreprocessorDirective
{
	private String myValue;

	public RegionPreprocessorDirective(CharSequence value)
	{
		myValue = value.toString();
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("RegionPreprocessorDirective{");
		sb.append("myValue='").append(myValue).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
