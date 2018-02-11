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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpAwaitExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpExpressionWithOperatorImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPostfixExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPrefixExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 09.08.2015
 */
public class CS0201 extends CompilerCheck<CSharpExpressionStatementImpl>
{
	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpExpressionStatementImpl element)
	{
		DotNetExpression expression = element.getExpression();
		if(expression instanceof CSharpPrefixExpressionImpl || expression instanceof CSharpPostfixExpressionImpl)
		{
			IElementType operatorElementType = ((CSharpExpressionWithOperatorImpl) expression).getOperatorElement().getOperatorElementType();
			if(operatorElementType != CSharpTokens.PLUSPLUS && operatorElementType != CSharpTokens.MINUSMINUS)
			{
				return newBuilder(element);
			}
			return null;
		}

		if(expression instanceof CSharpNewExpression)
		{
			// anonym object dont reported as illegal
			if(((CSharpNewExpression) expression).getFieldOrPropertySetBlock() != null)
			{
				return null;
			}
			if(((CSharpNewExpression) expression).getNewType() == null)
			{
				return newBuilder(element);
			}
			return null;
		}

		if(!(expression instanceof CSharpMethodCallExpressionImpl) &&
				!(expression instanceof CSharpAssignmentExpressionImpl) &&
				!(expression instanceof CSharpAwaitExpressionImpl))
		{
			return newBuilder(element);
		}

		return null;
	}
}
