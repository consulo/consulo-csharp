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

package consulo.csharp.impl.ide.parameterInfo;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.AccessToken;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpStaticTypeRef;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.CodeInsightSettings;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiNamedElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

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
	@RequiredReadAction
	public static ParameterPresentationBuilder<CSharpSimpleParameterInfo> build(@Nonnull CSharpSimpleLikeMethod callable, @Nonnull PsiElement scope)
	{
		CSharpSimpleParameterInfo[] parameters = callable.getParameterInfos();
		DotNetTypeRef returnType = callable.getReturnTypeRef();

		char[] bounds = getOpenAndCloseTokens(callable);

		ParameterPresentationBuilder<CSharpSimpleParameterInfo> builder = new ParameterPresentationBuilder<>();

		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			if(callable instanceof CSharpConstructorDeclaration)
			{
				builder.add(((CSharpConstructorDeclaration) callable).getName());
			}
			else if(callable instanceof PsiNamedElement)
			{
				builder.add(CSharpTypeRefPresentationUtil.buildShortText(returnType));

				builder.addSpace();

				builder.add(((PsiNamedElement) callable).getName());
			}

			builder.addEscaped(String.valueOf(bounds[0]));
		}

		if(parameters.length > 0)
		{
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					builder.add(", ");
				}

				CSharpSimpleParameterInfo parameter = parameters[i];
				try (AccessToken ignored = builder.beginParameter(i))
				{
					buildParameter(builder, parameter, scope);
				}
			}
		}
		else
		{
			try (AccessToken ignored = builder.beginParameter(0))
			{
				builder.add("<no parameters>");
			}
		}

		if(CodeInsightSettings.getInstance().SHOW_FULL_SIGNATURES_IN_PARAMETER_INFO)
		{
			builder.addEscaped(String.valueOf(bounds[1]));
		}

		return builder;
	}

	@RequiredReadAction
	private static void buildParameter(@Nonnull ParameterPresentationBuilder<CSharpSimpleParameterInfo> builder, @Nonnull CSharpSimpleParameterInfo o, @Nonnull PsiElement scope)
	{
		String text = CSharpTypeRefPresentationUtil.buildShortText(o.getTypeRef());
		builder.add(text);

		if(o.getTypeRef() != CSharpStaticTypeRef.__ARGLIST_TYPE)
		{
			builder.addSpace();
			String notNullName = o.getNotNullName();
			builder.add(notNullName);
		}

		PsiElement element = o.getElement();
		if(element instanceof DotNetVariable)
		{
			DotNetExpression initializer = ((DotNetVariable) element).getInitializer();
			if(initializer != null)
			{
				String initializerText = initializer.getText();
				builder.add(" = ");
				builder.addEscaped(initializerText);
			}
		}
	}
}
