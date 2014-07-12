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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionWithParameters;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 07.01.14.
 */
public class MethodAcceptorImpl
{
	private static interface MethodAcceptor
	{
		int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters);
	}

	private static class SimpleMethodAcceptor implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters)
		{
			int weight = 0;
			for(int i = 0; i < expressions.length; i++)
			{
				DotNetExpression expression = expressions[i];
				DotNetParameter parameter = ArrayUtil2.safeGet(parameters, i);
				if(parameter == null)
				{
					return weight;
				}

				DotNetTypeRef expressionType = expression.toTypeRef(false);
				DotNetTypeRef parameterType = parameter.toTypeRef(false);

				if(CSharpTypeUtil.isInheritable(parameterType, expressionType, scope))
				{
					weight++;
				}
				else
				{
					return weight;
				}
			}

			return weight == parameters.length ? WeightProcessor.MAX_WEIGHT : weight;
		}
	}

	private static class MethodAcceptorWithParams implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters)
		{
			int weight = 0;
			for(int i = 0; i < expressions.length; i++)
			{
				DotNetExpression expression = expressions[i];
				DotNetParameter parameter = ArrayUtil2.safeGet(parameters, i);

				DotNetTypeRef expressionType = expression.toTypeRef(false);

				DotNetTypeRef parameterType = null;
				if(parameter == null)
				{
					DotNetParameter lastParameter = ArrayUtil.getLastElement(parameters);
					if(lastParameter == null || !lastParameter.hasModifier(CSharpModifier.PARAMS))
					{
						return weight;
					}
					parameterType = CSharpResolveUtil.resolveIterableType(scope, lastParameter.toTypeRef(false));
				}
				else
				{
					parameterType = parameter.toTypeRef(false);
				}

				if(CSharpTypeUtil.isInheritable(parameterType, expressionType, scope))
				{
					weight++;
				}
				else
				{
					if(parameter != null && parameter.hasModifier(CSharpModifier.PARAMS))
					{
						parameterType = CSharpResolveUtil.resolveIterableType(scope, parameterType);
						if(CSharpTypeUtil.isInheritable(parameterType, expressionType, scope))
						{
							weight ++;
							continue;
						}
					}

					return weight;
				}
			}

			return weight == expressions.length ? WeightProcessor.MAX_WEIGHT : weight;
		}
	}

	private static class MethodAcceptorWithDefaultValues implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters)
		{
			if(expressions.length >= parameters.length)
			{
				return 0;
			}

			for(int i = 0; i < parameters.length; i++)
			{
				DotNetExpression expression = ArrayUtil2.safeGet(expressions, i);
				DotNetParameter parameter = parameters[i];

				// if expression no found - but parameter have default value - it value
				if(expression == null && parameter.getInitializer() != null)
				{
					continue;
				}

				if(expression == null)
				{
					return 0;
				}

				DotNetTypeRef expressionType = expression.toTypeRef(false);
				DotNetTypeRef parameterType = parameter.toTypeRef(false);

				if(!CSharpTypeUtil.isInheritable(parameterType, expressionType, scope))
				{
					return 0;
				}
			}

			return WeightProcessor.MAX_WEIGHT;
		}
	}

	private static final MethodAcceptor[] ourAcceptors = new MethodAcceptor[]{
			new SimpleMethodAcceptor(),
			new MethodAcceptorWithParams(),
			new MethodAcceptorWithDefaultValues()
	};

	public static int calcAcceptableWeight(PsiElement scope, CSharpExpressionWithParameters withParameters,
			DotNetParameterListOwner parameterListOwner)
	{
		DotNetParameter[] parameters = parameterListOwner.getParameters();
		return calcAcceptableWeight(scope, withParameters.getParameterExpressions(), parameters);
	}

	public static int calcAcceptableWeight(PsiElement scope, CSharpExpressionWithParameters withParameters, DotNetParameter[] parameters)
	{
		return calcAcceptableWeight(scope, withParameters.getParameterExpressions(), parameters);
	}

	public static int calcAcceptableWeight(PsiElement scope, DotNetExpression[] parameterExpressions, DotNetParameterListOwner parameterListOwner)
	{
		DotNetParameter[] netParameters = parameterListOwner.getParameters();
		return calcAcceptableWeight(scope, parameterExpressions, netParameters);
	}

	public static int calcAcceptableWeight(PsiElement scope, DotNetExpression[] parameterExpressions, DotNetParameter[] parameters)
	{
		int weight = 0;
		for(MethodAcceptor ourAcceptor : ourAcceptors)
		{
			int calculatedWeight = ourAcceptor.calcAcceptableWeight(scope, parameterExpressions, parameters);
			if(calculatedWeight == WeightProcessor.MAX_WEIGHT)
			{
				return WeightProcessor.MAX_WEIGHT;
			}
			else
			{
				weight += calculatedWeight;
			}
		}
		return weight;
	}
}
