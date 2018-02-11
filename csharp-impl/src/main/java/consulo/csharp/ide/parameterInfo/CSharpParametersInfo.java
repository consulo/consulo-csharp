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

package consulo.csharp.ide.parameterInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.impl.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.UnfairTextRange;
import com.intellij.psi.PsiElement;
import com.intellij.xml.util.XmlStringUtil;

/**
 * @author VISTALL
 * @since 11.05.14
 */
public class CSharpParametersInfo
{
	public static final char[] ourBrackets = {
			'[',
			']'
	};
	public static final char[] ourParentheses = {
			'(',
			')'
	};

	private static final TextRange EMPTY = new UnfairTextRange(-1, -1);

	@Nonnull
	public static char[] getOpenAndCloseTokens(@Nullable Object callable)
	{
		if(callable instanceof CSharpIndexAccessExpressionImpl || callable instanceof CSharpIndexMethodDeclaration)
		{
			return ourBrackets;
		}
		return ourParentheses;
	}

	@Nonnull
	public static CSharpParametersInfo build(@Nonnull CSharpSimpleLikeMethod callable, @Nonnull PsiElement scope)
	{
		CSharpSimpleParameterInfo[] parameters = callable.getParameterInfos();
		DotNetTypeRef returnType = callable.getReturnTypeRef();

		char[] bounds = getOpenAndCloseTokens(callable);

		int length = 0;

		CSharpParametersInfo parametersInfo = new CSharpParametersInfo(parameters.length);
		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			String textOfReturnType = CSharpTypeRefPresentationUtil.buildShortText(returnType, scope);
			parametersInfo.myBuilder.append(textOfReturnType);
			parametersInfo.myBuilder.append(" ").append(bounds[0]);

			length = XmlStringUtil.escapeString(textOfReturnType).length() + 2; // space and brace
		}

		if(parameters.length > 0)
		{
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					length += parametersInfo.appendComma();
				}

				final int startOffset = length;
				CSharpSimpleParameterInfo parameter = parameters[i];
				length += parametersInfo.buildParameter(parameter, scope);
				parametersInfo.myParameterRanges[i] = new TextRange(startOffset, length);
			}
		}
		else
		{
			String text = "<no parameters>";
			parametersInfo.myBuilder.append(text);
			parametersInfo.myParameterRanges[0] = new TextRange(length, length + XmlStringUtil.escapeString(text).length());
		}

		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			parametersInfo.myBuilder.append(bounds[1]);
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

	@RequiredReadAction
	private int buildParameter(@Nonnull CSharpSimpleParameterInfo o, @Nonnull PsiElement scope)
	{
		String text = CSharpTypeRefPresentationUtil.buildShortText(o.getTypeRef(), scope);
		myBuilder.append(text);
		int nameOffset = 0;
		if(o.getTypeRef() != CSharpStaticTypeRef.__ARGLIST_TYPE)
		{
			myBuilder.append(" ");
			String notNullName = o.getNotNullName();
			myBuilder.append(notNullName);
			nameOffset += notNullName.length() + 1;
		}

		PsiElement element = o.getElement();
		if(element instanceof DotNetVariable)
		{
			DotNetExpression initializer = ((DotNetVariable) element).getInitializer();
			if(initializer != null)
			{
				String initializerText = initializer.getText();
				myBuilder.append(" = ").append(initializerText);
				nameOffset += initializerText.length() + 3;
			}
		}
		return XmlStringUtil.escapeString(text).length() + nameOffset;
	}

	@Nonnull
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
		if(myParameterCount == 0)
		{
			return EMPTY;
		}
		TextRange textRange = ArrayUtil2.safeGet(myParameterRanges, i);
		return textRange == null ? EMPTY : textRange;
	}

	private int appendComma()
	{
		myBuilder.append(", ");
		return 2;
	}

	@Nonnull
	public String getText()
	{
		return myBuilder.toString();
	}
}
