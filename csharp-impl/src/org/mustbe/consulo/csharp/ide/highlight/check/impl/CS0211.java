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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPrefixExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 13.03.2016
 */
public class CS0211 extends CompilerCheck<CSharpPrefixExpressionImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpPrefixExpressionImpl element)
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
		return super.checkImpl(languageVersion, element);
	}
}
