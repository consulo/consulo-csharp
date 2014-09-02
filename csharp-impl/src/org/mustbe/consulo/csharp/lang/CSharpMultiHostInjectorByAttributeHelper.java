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

package org.mustbe.consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.dotnet.lang.MultiHostInjectorByAttributeHelper;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 02.09.14
 */
public class CSharpMultiHostInjectorByAttributeHelper implements MultiHostInjectorByAttributeHelper
{
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

	@Nullable
	@Override
	public TextRange getTextRangeForInject(@NotNull DotNetExpression expression)
	{
		if(expression instanceof CSharpConstantExpressionImpl)
		{
			IElementType literalType = ((CSharpConstantExpressionImpl) expression).getLiteralType();
			if(literalType == CSharpTokens.VERBATIM_STRING_LITERAL)
			{
				return new TextRange(2, expression.getTextLength());
			}
			else if(literalType == CSharpTokens.STRING_LITERAL)
			{
				return new TextRange(1, expression.getTextLength());
			}
		}
		return null;
	}
}
