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
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.SimpleGenericExtractorImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
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
		int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters, DotNetGenericExtractor
				extractor);
	}

	private static class SimpleMethodAcceptor implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters,
				DotNetGenericExtractor extractor)
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
				DotNetTypeRef parameterType = calcParameterTypeRef(scope, parameter, extractor);

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
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters,
				DotNetGenericExtractor extractor)
		{
			DotNetParameter lastParameter = ArrayUtil.getLastElement(parameters);
			if(lastParameter == null || !lastParameter.hasModifier(CSharpModifier.PARAMS))
			{
				return 0;
			}

			int weight = 0;
			for(int i = 0; i < expressions.length; i++)
			{
				DotNetExpression expression = expressions[i];
				DotNetParameter parameter = ArrayUtil2.safeGet(parameters, i);

				DotNetTypeRef expressionType = expression.toTypeRef(false);

				DotNetTypeRef parameterType = null;
				if(parameter == null)
				{
					parameterType = CSharpResolveUtil.resolveIterableType(scope, calcParameterTypeRef(scope, lastParameter, extractor));
				}
				else
				{
					parameterType = calcParameterTypeRef(scope, parameter, extractor);
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
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetParameter[] parameters, DotNetGenericExtractor extractor)
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
				DotNetTypeRef parameterType = calcParameterTypeRef(scope, parameter, extractor);

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

	public static int calcAcceptableWeight(PsiElement scope, CSharpCallArgumentListOwner owner, DotNetLikeMethodDeclaration declaration)
	{
		DotNetExpression[] parameterExpressions = owner.getParameterExpressions();
		return calcAcceptableWeight(scope, parameterExpressions, createExtractorFromCall(owner, declaration), declaration);
	}

	public static int calcAcceptableWeight(PsiElement scope, DotNetExpression[] parameterExpressions,
			DotNetGenericExtractor extractor, DotNetLikeMethodDeclaration declaration)
	{
		DotNetParameter[] parameters = declaration.getParameters();

		int weight = 0;
		for(MethodAcceptor ourAcceptor : ourAcceptors)
		{
			int calculatedWeight = ourAcceptor.calcAcceptableWeight(scope, parameterExpressions, parameters, extractor);
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

	@NotNull
	public static DotNetGenericExtractor createExtractorFromCall(CSharpCallArgumentListOwner owner, DotNetGenericParameterListOwner genericOwner)
	{
		DotNetTypeList typeArgumentList = owner.getTypeArgumentList();
		DotNetTypeRef[] typeArguments = DotNetTypeRef.EMPTY_ARRAY;
		if(typeArgumentList != null)
		{
			DotNetGenericParameter[] genericParameters = genericOwner.getGenericParameters();
			typeArguments = typeArgumentList.getTypeRefs();
			if(typeArguments.length > 0 && genericParameters.length == typeArguments.length)
			{
				return new SimpleGenericExtractorImpl(genericParameters, typeArguments);
			}
		}

		return DotNetGenericExtractor.EMPTY;
	}

	public static DotNetTypeRef calcParameterTypeRef(PsiElement scope, DotNetParameter parameter, DotNetGenericExtractor extractor)
	{
		return GenericUnwrapTool.exchangeTypeRefs(parameter.toTypeRef(false), extractor, scope);
	}
}
