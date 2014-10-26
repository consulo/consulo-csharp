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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.SimpleGenericExtractorImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
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
		int calcAcceptableWeight(@NotNull PsiElement scope, DotNetTypeRef[] expressionTypes, DotNetParameter[] parameters);
	}

	private static class SimpleMethodAcceptor implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetTypeRef[] expressionTypes, DotNetParameter[] parameters)
		{
			DotNetTypeRef[] types = new DotNetTypeRef[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				types[i] = parameter.toTypeRef(false);
			}
			return calcSimpleAcceptableWeight(scope, expressionTypes, types);
		}
	}

	public static int calcSimpleAcceptableWeight(@NotNull PsiElement scope, DotNetExpression[] expressions, DotNetTypeRef[] parameters)
	{
		return calcSimpleAcceptableWeight(scope, convertExpressionsToTypeRefs(expressions), parameters);
	}

	public static int calcSimpleAcceptableWeight(@NotNull PsiElement scope, DotNetTypeRef[] expressions, DotNetTypeRef[] parameters)
	{
		int weight = 0;
		for(int i = 0; i < expressions.length; i++)
		{
			DotNetTypeRef expressionTypeRef = expressions[i];
			DotNetTypeRef parameterTypeRef = ArrayUtil2.safeGet(parameters, i);
			if(parameterTypeRef == null)
			{
				return weight;
			}

			if(CSharpTypeUtil.isInheritableWithImplicit(parameterTypeRef, expressionTypeRef, scope))
			{
				weight++;
			}
			else
			{
				return weight;
			}
		}

		return weight == parameters.length ? WeightUtil.MAX_WEIGHT : weight;
	}

	private static class MethodAcceptorWithParams implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetTypeRef[] expressionTypes, DotNetParameter[] parameters)
		{
			DotNetParameter lastParameter = ArrayUtil.getLastElement(parameters);
			if(lastParameter == null || !lastParameter.hasModifier(CSharpModifier.PARAMS))
			{
				return 0;
			}

			int weight = 0;
			for(int i = 0; i < expressionTypes.length; i++)
			{
				DotNetTypeRef expressionType = expressionTypes[i];
				DotNetParameter parameter = ArrayUtil2.safeGet(parameters, i);

				DotNetTypeRef parameterType = null;
				if(parameter == null)
				{
					parameterType = CSharpResolveUtil.resolveIterableType(scope, lastParameter.toTypeRef(false));
				}
				else
				{
					parameterType = parameter.toTypeRef(false);
				}

				if(CSharpTypeUtil.isInheritableWithImplicit(parameterType, expressionType, scope))
				{
					weight++;
				}
				else
				{
					if(parameter != null && parameter.hasModifier(CSharpModifier.PARAMS))
					{
						parameterType = CSharpResolveUtil.resolveIterableType(scope, parameterType);
						if(CSharpTypeUtil.isInheritableWithImplicit(parameterType, expressionType, scope))
						{
							weight++;
							continue;
						}
					}

					return weight;
				}
			}

			return weight == expressionTypes.length ? WeightUtil.MAX_WEIGHT : weight;
		}
	}

	private static class MethodAcceptorWithDefaultValues implements MethodAcceptor
	{
		@Override
		public int calcAcceptableWeight(@NotNull PsiElement scope, DotNetTypeRef[] expressionTypes, DotNetParameter[] parameters)
		{
			if(expressionTypes.length >= parameters.length)
			{
				return 0;
			}

			for(int i = 0; i < parameters.length; i++)
			{
				DotNetTypeRef expressionType = ArrayUtil2.safeGet(expressionTypes, i);
				DotNetParameter parameter = parameters[i];

				// if expression no found - but parameter have default value - it value
				if(expressionType == null && parameter.getInitializer() != null)
				{
					continue;
				}

				if(expressionType == null)
				{
					return 0;
				}

				DotNetTypeRef parameterType = parameter.toTypeRef(false);

				if(!CSharpTypeUtil.isInheritableWithImplicit(parameterType, expressionType, scope))
				{
					return 0;
				}
			}

			return WeightUtil.MAX_WEIGHT;
		}
	}

	private static final MethodAcceptor[] ourAcceptors = new MethodAcceptor[]{
			new SimpleMethodAcceptor(),
			new MethodAcceptorWithParams(),
			new MethodAcceptorWithDefaultValues()
	};

	public static int calcAcceptableWeight(PsiElement scope, CSharpCallArgumentListOwner owner, DotNetLikeMethodDeclaration declaration)
	{
		return calcAcceptableWeight(scope, owner.getParameterExpressions(), declaration);
	}

	public static int calcAcceptableWeight(PsiElement scope, DotNetExpression[] parameterExpressions, DotNetLikeMethodDeclaration declaration)
	{
		DotNetTypeRef[] expressionTypeRefs = convertExpressionsToTypeRefs(parameterExpressions);

		DotNetParameter[] parameters = declaration.getParameters();

		int weight = 0;
		for(MethodAcceptor ourAcceptor : ourAcceptors)
		{
			int calculatedWeight = ourAcceptor.calcAcceptableWeight(scope, expressionTypeRefs, parameters);
			if(calculatedWeight == WeightUtil.MAX_WEIGHT)
			{
				return WeightUtil.MAX_WEIGHT;
			}
			else
			{
				weight += calculatedWeight;
			}
		}
		return weight;
	}

	@NotNull
	private static DotNetTypeRef[] convertExpressionsToTypeRefs(@NotNull DotNetExpression[] parameterExpressions)
	{
		DotNetTypeRef[] expressionTypeRefs = new DotNetTypeRef[parameterExpressions.length];
		for(int i = 0; i < parameterExpressions.length; i++)
		{
			DotNetExpression expression = parameterExpressions[i];
			expressionTypeRefs[i] = expression.toTypeRef(false);
		}
		return expressionTypeRefs;
	}

	@NotNull
	public static DotNetGenericExtractor createExtractorFromCall(CSharpCallArgumentListOwner owner, DotNetGenericParameterListOwner genericOwner)
	{
		return createExtractorFromCall(owner.getTypeArgumentListRefs(), genericOwner);
	}

	@NotNull
	public static DotNetGenericExtractor createExtractorFromCall(DotNetTypeRef[] typeArguments, DotNetGenericParameterListOwner genericOwner)
	{
		DotNetGenericParameter[] genericParameters = genericOwner.getGenericParameters();
		if(typeArguments.length > 0 && genericParameters.length == typeArguments.length)
		{
			return new SimpleGenericExtractorImpl(genericParameters, typeArguments);
		}

		return DotNetGenericExtractor.EMPTY;
	}
}
