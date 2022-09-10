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
public class IfPreprocessorDirective extends PreprocessorDirective
{
	private String myValue;
	private boolean myElseIf;

	public IfPreprocessorDirective(String value, boolean elseIf)
	{
		myValue = value;
		myElseIf = elseIf;
	}

	public boolean isElseIf()
	{
		return myElseIf;
	}

	public String getValue()
	{
		return myValue;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("IfPreprocessorDirective{");
		sb.append("myValue='").append(myValue).append('\'');
		sb.append(", myElseIf=").append(myElseIf);
		sb.append('}');
		return sb.toString();
	}
}
