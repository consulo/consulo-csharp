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

package consulo.csharp.lang.parser.preprocessor;

import java.util.Set;

/**
 * @author VISTALL
 * @since 06-Nov-17
 */
public class PragmaWarningPreprocessorDirective extends PreprocessorDirective
{
	private String myType;
	private String myAction;
	private Set<String> myArguments;

	public PragmaWarningPreprocessorDirective(String type, String action, Set<String> arguments)
	{
		myType = type;
		myAction = action;
		myArguments = arguments;
	}

	public String getAction()
	{
		return myAction;
	}

	public String getType()
	{
		return myType;
	}

	public Set<String> getArguments()
	{
		return myArguments;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("PragmaWarningPreprocessorDirective{");
		sb.append("myType='").append(myType).append('\'');
		sb.append(", myAction='").append(myAction).append('\'');
		sb.append(", myArguments=").append(myArguments);
		sb.append('}');
		return sb.toString();
	}
}
