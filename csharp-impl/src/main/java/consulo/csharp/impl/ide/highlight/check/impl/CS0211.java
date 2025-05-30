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

package consulo.csharp.impl.ide.highlight.check.impl;

import jakarta.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.impl.psi.source.CSharpPrefixExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.ast.IElementType;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 13.03.2016
 */
public class CS0211 extends CompilerCheck<CSharpPrefixExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpPrefixExpressionImpl element)
	{
		IElementType operatorElementType = element.getOperatorElement().getOperatorElementType();
		if(operatorElementType == CSharpTokens.AND)
		{
			CSharpCallArgument callArgument = element.getCallArguments()[0];
			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression instanceof CSharpNewExpression)
			{
				return newBuilder(argumentExpression);
			}
		}
		return super.checkImpl(languageVersion, highlightContext, element);
	}
}
