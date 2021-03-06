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

package consulo.csharp.lang;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.util.TextRange;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.dotnet.lang.MultiHostInjectorByAttributeHelper;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 02.09.14
 */
public class CSharpMultiHostInjectorByAttributeHelper implements MultiHostInjectorByAttributeHelper
{
	@Nullable
	@Override
	public String getLanguageId(@Nonnull DotNetAttribute attribute)
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
	@RequiredReadAction
	public TextRange getTextRangeForInject(@Nonnull DotNetExpression expression)
	{
		if(expression instanceof CSharpConstantExpressionImpl)
		{
			return CSharpConstantExpressionImpl.getStringValueTextRange((CSharpConstantExpressionImpl) expression);
		}
		return null;
	}
}
