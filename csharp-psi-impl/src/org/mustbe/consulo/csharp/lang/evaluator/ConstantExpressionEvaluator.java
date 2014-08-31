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

package org.mustbe.consulo.csharp.lang.evaluator;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 28.08.14
 */
public class ConstantExpressionEvaluator extends CSharpElementVisitor
{
	private Object myValue;

	public ConstantExpressionEvaluator(DotNetExpression expression)
	{
		expression.accept(this);
	}

	@Override
	public void visitConstantExpression(CSharpConstantExpressionImpl expression)
	{
		myValue = expression.getValue();
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T> T getValueAs(Class<T> clazz)
	{
		Object object = getValue();
		if(clazz.isInstance(object))
		{
			return (T) object;
		}
		return null;
	}

	public Object getValue()
	{
		return myValue;
	}
}
