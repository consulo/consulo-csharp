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

package consulo.csharp.lang.impl.evaluator;

import consulo.component.ProcessCanceledException;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.impl.psi.source.CSharpAsExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpTypeCastExpressionImpl;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.08.14
 */
public class ConstantExpressionEvaluator extends CSharpElementVisitor
{
	private Object myValue;

	public ConstantExpressionEvaluator(@Nullable DotNetExpression expression)
	{
		if(expression != null)
		{
			expression.accept(this);
		}
	}

	@Override
	public void visitConstantExpression(CSharpConstantExpressionImpl expression)
	{
		try
		{
			myValue = expression.getValue();
		}
		catch(ProcessCanceledException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			//
		}
	}

	@Override
	public void visitBinaryExpression(CSharpBinaryExpressionImpl expression)
	{
		DotNetExpression leftExpression = expression.getLeftExpression();
		DotNetExpression rightExpression = expression.getRightExpression();
		if(leftExpression == null || rightExpression == null)
		{
			return;
		}

		Object leftValue = new ConstantExpressionEvaluator(leftExpression).getValue();
		Object rightValue = new ConstantExpressionEvaluator(rightExpression).getValue();
		if(leftValue == null || rightValue == null)
		{
			return;
		}

		myValue = OperatorEvaluator.calcBinary(expression.getOperatorElement().getOperatorElementType(), leftValue, rightValue);
	}

	@Override
	public void visitTypeCastExpression(CSharpTypeCastExpressionImpl expression)
	{
		myValue = castTo(new ConstantExpressionEvaluator(expression.getInnerExpression()).getValue(), expression);
	}

	@Override
	public void visitAsExpression(CSharpAsExpressionImpl expression)
	{
		myValue = castTo(new ConstantExpressionEvaluator(expression.getInnerExpression()).getValue(), expression);
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		PsiElement resolvedElement = expression.resolve();
		if(resolvedElement instanceof DotNetVariable)
		{
			if(((DotNetVariable) resolvedElement).isConstant() || ((DotNetVariable) resolvedElement).hasModifier(CSharpModifier.READONLY))
			{
				DotNetExpression initializer = ((DotNetVariable) resolvedElement).getInitializer();
				if(initializer == null)
				{
					return;
				}
				myValue = new ConstantExpressionEvaluator(initializer).getValue();
			}
		}
	}

	private static Object castTo(Object value, DotNetExpression element)
	{
		if(value == null)
		{
			return null;
		}
		DotNetTypeRef typeRef = element.toTypeRef(false);
		PsiElement psiElement = typeRef.resolve().getElement();
		if(!(psiElement instanceof DotNetTypeDeclaration))
		{
			return value;
		}

		String vmQName = ((DotNetTypeDeclaration) psiElement).getVmQName();
		if(DotNetTypes.System.Int32.equals(vmQName))
		{
			if(value instanceof Number)
			{
				return ((Number) value).intValue();
			}
		}
		else if(DotNetTypes.System.Int16.equals(vmQName))
		{
			if(value instanceof Number)
			{
				return ((Number) value).shortValue();
			}
		}
		else if(DotNetTypes.System.Int64.equals(vmQName))
		{
			if(value instanceof Number)
			{
				return ((Number) value).longValue();
			}
		}
		else if(DotNetTypes.System.SByte.equals(vmQName))
		{
			if(value instanceof Number)
			{
				return ((Number) value).byteValue();
			}
		}
		else if(DotNetTypes.System.Single.equals(vmQName))
		{
			if(value instanceof Number)
			{
				return ((Number) value).floatValue();
			}
		}
		else if(DotNetTypes.System.Double.equals(vmQName))
		{
			if(value instanceof Number)
			{
				return ((Number) value).doubleValue();
			}
		}
		else if(DotNetTypes.System.String.equals(vmQName))
		{
			return value.toString();
		}
		return value;
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
