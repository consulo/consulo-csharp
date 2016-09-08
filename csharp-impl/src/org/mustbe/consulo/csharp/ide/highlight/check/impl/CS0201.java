/*
 * Copyright 2013-2015 must-be.org
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
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.CSharpHighlightContext;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.CSharpNewExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAwaitExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionWithOperatorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPostfixExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPrefixExpressionImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
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
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpExpressionStatementImpl element)
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
