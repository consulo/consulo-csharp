/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.parameterInfo;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class CSharpGenericParametersInfo
{
	private static final TextRange EMPTY = new UnfairTextRange(-1, -1);

	public static CSharpGenericParametersInfo build(@NotNull DotNetGenericParameterListOwner parameterListOwner)
	{
		DotNetGenericParameter[] genericParameters = parameterListOwner.getGenericParameters();

		CSharpGenericParametersInfo parametersInfo = new CSharpGenericParametersInfo(genericParameters.length);

		if(genericParameters.length > 0)
		{
			for(int i = 0; i < genericParameters.length; i++)
			{
				if(i != 0)
				{
					parametersInfo.appendComma();
				}

				DotNetGenericParameter parameter = genericParameters[i];

				int length = parametersInfo.length();
				parametersInfo.myBuilder.append(parameter.getName());
				parametersInfo.myParameterRanges[i] = new TextRange(length, parametersInfo.length());
			}
		}
		return parametersInfo;
	}

	private TextRange[] myParameterRanges;
	private int myParameterCount;
	private StringBuilder myBuilder = new StringBuilder();

	public CSharpGenericParametersInfo(int count)
	{
		myParameterCount = count;
		myParameterRanges = new TextRange[myParameterCount == 0 ? 1 : myParameterCount];
	}

	public int length()
	{
		return myBuilder.length();
	}

	@NotNull
	public TextRange getParameterRange(int i)
	{
		if(i == -1)
		{
			if(myParameterCount == 0)
			{
				return myParameterRanges[0];
			}
			return EMPTY;
		}
		TextRange textRange = ArrayUtil2.safeGet(myParameterRanges, i);
		return textRange == null ? EMPTY : textRange;
	}

	private void appendComma()
	{
		myBuilder.append(", ");
	}

	@NotNull
	public String getText()
	{
		return myBuilder.toString();
	}
}
