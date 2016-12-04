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

package consulo.csharp.lang;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiLanguageInjectionHost;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.dotnet.lang.MultiHostInjectorByAttributeHelper;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 02.09.14
 */
public class CSharpMultiHostInjectorByAttributeHelper implements MultiHostInjectorByAttributeHelper
{
	@RequiredReadAction
	@Nullable
	@Override
	public String getLanguageId(@NotNull DotNetAttribute attribute)
	{
		if(!(attribute instanceof CSharpAttribute))
		{
			return null;
		}
		DotNetExpression[] parameterExpressions = ((CSharpAttribute) attribute).getParameterExpressions();
		if(parameterExpressions.length == 0)
		{
			return null;
		}
		return new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
	}

	@RequiredReadAction
	@Override
	public void fillExpressionsForInject(@NotNull DotNetExpression expression, @NotNull Consumer<Pair<PsiLanguageInjectionHost, TextRange>> list)
	{
		if(expression instanceof CSharpConstantExpressionImpl)
		{
			processConstantExpression(expression, list);
		}
		else if(expression instanceof CSharpBinaryExpressionImpl)
		{
			processBinaryExpression((CSharpBinaryExpressionImpl) expression, list);
		}
	}

	@RequiredReadAction
	private void processBinaryExpression(@NotNull CSharpBinaryExpressionImpl expression,
			@NotNull Consumer<Pair<PsiLanguageInjectionHost, TextRange>> list)
	{
		for(DotNetExpression exp : expression.getParameterExpressions())
		{
			if(exp instanceof CSharpConstantExpressionImpl)
			{
				processConstantExpression(exp, list);
			}
			else if(exp instanceof CSharpBinaryExpressionImpl)
			{
				processBinaryExpression((CSharpBinaryExpressionImpl) exp, list);
			}
		}
	}

	@RequiredReadAction
	private void processConstantExpression(@NotNull DotNetExpression expression, @NotNull Consumer<Pair<PsiLanguageInjectionHost, TextRange>> list)
	{
		TextRange textRange = null;
		IElementType literalType = ((CSharpConstantExpressionImpl) expression).getLiteralType();
		if(literalType == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			textRange = new TextRange(2, expression.getTextLength() - 1);
		}
		else if(literalType == CSharpTokens.STRING_LITERAL)
		{
			textRange = new TextRange(1, expression.getTextLength() - 1);
		}

		if(textRange == null)
		{
			return;
		}
		list.accept(Pair.create((PsiLanguageInjectionHost) expression, textRange));
	}
}
