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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class CSharpParametersInfo
{
	private static final TextRange EMPTY = new UnfairTextRange(-1, -1);

	public static CSharpParametersInfo build(Object o)
	{
		//noinspection unchecked
		Pair<Object, DotNetGenericExtractor> pair = (Pair<Object, DotNetGenericExtractor>) o;

		Object callable = pair.getFirst();

		Object[] parameters = null;
		DotNetTypeRef returnType = null;
		if(callable instanceof DotNetLikeMethodDeclaration)
		{
			parameters = ((DotNetLikeMethodDeclaration) callable).getParameters();
			returnType = ((DotNetLikeMethodDeclaration) callable).getReturnTypeRef();
		}
		else if(callable instanceof CSharpLambdaTypeRef)
		{
			parameters = ((CSharpLambdaTypeRef) callable).getParameterTypes();
			returnType = ((CSharpLambdaTypeRef) callable).getReturnType();
		}

		if(parameters == null)
		{
			return null;
		}

		CSharpParametersInfo parametersInfo = new CSharpParametersInfo(parameters.length);
		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			parametersInfo.myBuilder.append(returnType.getPresentableText());
			parametersInfo.myBuilder.append(" (");
		}

		if(parameters.length > 0)
		{
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					parametersInfo.appendComma();
				}

				Object parameter = parameters[i];

				int length = parametersInfo.length();
				parametersInfo.buildParameter(parameter, pair.getSecond(), i);
				parametersInfo.myParameterRanges[i] = new TextRange(length, parametersInfo.length());
			}
		}
		else
		{
			int length = parametersInfo.length();
			parametersInfo.myBuilder.append("<no parameters>");
			parametersInfo.myParameterRanges[0] = new TextRange(length, parametersInfo.length() + 6); //escaping
		}

		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			parametersInfo.myBuilder.append(")");
		}

		return parametersInfo;
	}

	private TextRange[] myParameterRanges;
	private int myParameterCount;
	private StringBuilder myBuilder = new StringBuilder();

	public CSharpParametersInfo(int count)
	{
		myParameterCount = count;
		myParameterRanges = new TextRange[myParameterCount == 0 ? 1 : myParameterCount];
	}

	private void buildParameter(Object o, DotNetGenericExtractor extractor, int index)
	{
		DotNetTypeRef typeRef = DotNetTypeRef.UNKNOWN_TYPE;
		String name = null;
		if(o instanceof DotNetParameter)
		{
			typeRef = MethodAcceptorImpl.calcParameterTypeRef((DotNetParameter)o, (DotNetParameter) o, extractor);
			name = ((DotNetParameter) o).getName();
		}
		else if(o instanceof DotNetTypeRef)
		{
			typeRef = (DotNetTypeRef) o;
			name = "p" + index;
		}

		myBuilder.append(typeRef.getPresentableText());
		myBuilder.append(" ");
		myBuilder.append(name);
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
