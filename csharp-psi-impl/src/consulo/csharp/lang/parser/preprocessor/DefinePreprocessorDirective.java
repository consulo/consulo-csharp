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

package consulo.csharp.lang.parser.preprocessor;

/**
 * @author VISTALL
 * @since 02.03.2016
 */
public class DefinePreprocessorDirective extends PreprocessorDirective
{
	private String myVariable;
	private boolean myUndef;

	public DefinePreprocessorDirective(String variable, boolean undef)
	{
		myVariable = variable;
		myUndef = undef;
	}

	public String getVariable()
	{
		return myVariable;
	}

	public boolean isUndef()
	{
		return myUndef;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("DefineVariable{");
		sb.append("myVariable='").append(myVariable).append('\'');
		sb.append(", myUndef=").append(myUndef);
		sb.append('}');
		return sb.toString();
	}
}
