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

package consulo.csharp.lang.impl.psi.source.resolve.genericInference;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.RecursionManager;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.*;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import consulo.util.collection.SmartList;
import consulo.util.dataholder.Key;
import consulo.util.lang.Couple;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author VISTALL
 * @since 29.10.14
 */
public class GenericInferenceUtil
{
	public static final Key<GenericInferenceResult> INFERENCE_RESULT = Key.create("inference.result");

	public static class GenericInferenceResult
	{
		private boolean mySuccess;
		private DotNetGenericExtractor myExtractor;

		public GenericInferenceResult(boolean success, @Nonnull DotNetGenericExtractor extractor)
		{
			mySuccess = success;
			myExtractor = extractor;
		}

		public boolean isSuccess()
		{
			return mySuccess;
		}

		@Nonnull
		public DotNetGenericExtractor getExtractor()
		{
			return myExtractor;
		}
	}

	@Nonnull
	@RequiredReadAction
	public static GenericInferenceResult inferenceGenericExtractor(@Nonnull PsiElement referenceElement,
																   @Nonnull CSharpCallArgumentListOwner callArgumentListOwner,
																   @Nonnull DotNetLikeMethodDeclaration methodDeclaration)
	{
		CSharpCallArgument[] arguments = callArgumentListOwner.getCallArguments();

		DotNetTypeRef[] typeArgumentListRef = DotNetTypeRef.EMPTY_ARRAY;
		if(referenceElement instanceof CSharpReferenceExpression)
		{
			typeArgumentListRef = ((CSharpReferenceExpression) referenceElement).getTypeArgumentListRefs();
		}
		return inferenceGenericExtractor(arguments, typeArgumentListRef, callArgumentListOwner, methodDeclaration);
	}

	@Nonnull
	@RequiredReadAction
	public static GenericInferenceResult inferenceGenericExtractor(@Nonnull CSharpCallArgument[] callArguments,
																   @Nonnull DotNetTypeRef[] typeArgumentListRefs,
																   @Nonnull PsiElement scopeElement,
																   @Nonnull DotNetLikeMethodDeclaration methodDeclaration)
	{
		DotNetGenericParameter[] genericParameters = methodDeclaration.getGenericParameters();
		if(genericParameters.length == 0 || typeArgumentListRefs.length > 0)
		{
			DotNetGenericExtractor extractor = genericParameters.length != typeArgumentListRefs.length ? DotNetGenericExtractor.EMPTY : CSharpGenericExtractor.create(genericParameters,
					typeArgumentListRefs);
			return new GenericInferenceResult(genericParameters.length == typeArgumentListRefs.length, extractor);
		}

		List<NCallArgument> methodCallArguments = RecursionManager.doPreventingRecursion(methodDeclaration, false, () -> NCallArgumentBuilder.buildCallArguments(callArguments, methodDeclaration,
				scopeElement.getResolveScope()));

		if(ContainerUtil.isEmpty(methodCallArguments))
		{
			return new GenericInferenceResult(true, DotNetGenericExtractor.EMPTY);
		}

		final Map<DotNetGenericParameter, DotNetTypeRef> map = new HashMap<>();

		for(NCallArgument nCallArgument : methodCallArguments)
		{
			ProgressManager.checkCanceled();

			DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
			if(parameterTypeRef == null)
			{
				continue;
			}

			DotNetTypeRef expressionTypeRef = unwrapPossibleGenericTypeRefs(nCallArgument, parameterTypeRef, map);
			if(expressionTypeRef instanceof CSharpFastImplicitTypeRef)
			{
				DotNetTypeRef mirror = ((CSharpFastImplicitTypeRef) expressionTypeRef).doMirror(parameterTypeRef);
				if(mirror != null)
				{
					expressionTypeRef = mirror;
				}
			}

			DotNetTypeResolveResult parameterTypeResolveResult = parameterTypeRef.resolve();
			DotNetTypeResolveResult expressionTypeResolveResult = expressionTypeRef.resolve();

			if(parameterTypeResolveResult instanceof CSharpLambdaResolveResult && expressionTypeResolveResult instanceof CSharpLambdaResolveResult)
			{
				CSharpLambdaResolveResult pLambdaResolveResult = (CSharpLambdaResolveResult) parameterTypeResolveResult;
				CSharpLambdaResolveResult eLambdaResolveResult = (CSharpLambdaResolveResult) expressionTypeResolveResult;

				DotNetTypeRef[] pParameterTypeRefs = pLambdaResolveResult.getParameterTypeRefs();
				DotNetTypeRef[] eParameterTypeRefs = eLambdaResolveResult.getParameterTypeRefs();

				if(pParameterTypeRefs.length == eParameterTypeRefs.length)
				{
					for(int i = 0; i < eParameterTypeRefs.length; i++)
					{
						DotNetTypeRef pParameterTypeRef = pParameterTypeRefs[i];
						DotNetTypeRef eParameterTypeRef = eParameterTypeRefs[i];

						inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pParameterTypeRef, eParameterTypeRef, scopeElement);
					}
				}

				inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pLambdaResolveResult.getReturnTypeRef(), eLambdaResolveResult.getReturnTypeRef(), scopeElement);
			}

			if(parameterTypeResolveResult instanceof CSharpArrayTypeRef.ArrayResolveResult && expressionTypeResolveResult instanceof CSharpArrayTypeRef.ArrayResolveResult)
			{
				DotNetTypeRef pTypeRef = ((CSharpArrayTypeRef.ArrayResolveResult) parameterTypeResolveResult).getInnerTypeRef();
				DotNetTypeRef eTypeRef = ((CSharpArrayTypeRef.ArrayResolveResult) expressionTypeResolveResult).getInnerTypeRef();
				inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pTypeRef, eTypeRef, scopeElement);
			}

			inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, parameterTypeRef, expressionTypeRef, scopeElement);
		}

		return new GenericInferenceResult(genericParameters.length == map.size(), CSharpGenericExtractor.create(map));
	}

	@RequiredReadAction
	private static void inferenceGenericFromExpressionTypeRefAndParameterTypeRef(DotNetGenericParameter[] methodGenericParameters,
																				 Map<DotNetGenericParameter, DotNetTypeRef> map,
																				 DotNetTypeRef parameterTypeRef,
																				 DotNetTypeRef expressionTypeRef,
																				 PsiElement scope)
	{
		if(expressionTypeRef == DotNetTypeRef.AUTO_TYPE || expressionTypeRef == DotNetTypeRef.UNKNOWN_TYPE || expressionTypeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return;
		}

		ProgressManager.checkCanceled();

		DotNetTypeResolveResult parameterTypeResolveResult = parameterTypeRef.resolve();

		PsiElement parameterElement = parameterTypeResolveResult.getElement();
		for(DotNetGenericParameter genericParameter : methodGenericParameters)
		{
			if(map.containsKey(genericParameter))
			{
				continue;
			}

			if(genericParameter.isEquivalentTo(parameterElement))
			{
				map.put(genericParameter, expressionTypeRef);
				return;
			}
		}

		DotNetTypeResolveResult expressionResultResult = CSharpTypeUtil.findTypeRefFromExtends(expressionTypeRef, parameterTypeRef);
		if(expressionResultResult == null)
		{
			return;
		}

		PsiElement expressionElement = expressionResultResult.getElement();
		DotNetGenericExtractor expressionExtractor = expressionResultResult.getGenericExtractor();

		if(expressionElement instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] expressionGenericParameters = ((DotNetGenericParameterListOwner) expressionElement).getGenericParameters();

			tryToFindTypeArgumentsForParameters(methodGenericParameters, map, parameterTypeResolveResult, expressionExtractor, expressionGenericParameters);

			// inference ended
			if(map.size() == methodGenericParameters.length)
			{
				return;
			}

			// list of deep type arguments <expression, parameter>
			List<Couple<DotNetTypeResolveResult>> levels = new SmartList<>();

			pushTypeArgumentsDeep(levels, expressionGenericParameters, expressionExtractor, parameterTypeResolveResult.getGenericExtractor());

			for(Couple<DotNetTypeResolveResult> level : levels)
			{
				DotNetTypeResolveResult expressionTypeResult = level.getFirst();
				DotNetTypeResolveResult parameterTypeResult = level.getSecond();

				DotNetGenericParameter[] otherExpressionGenericParameters = ((DotNetTypeDeclaration) expressionTypeResult.getElement()).getGenericParameters();

				tryToFindTypeArgumentsForParameters(methodGenericParameters, map, parameterTypeResult, expressionTypeResult.getGenericExtractor(), otherExpressionGenericParameters);
			}
		}
	}

	@RequiredReadAction
	private static void tryToFindTypeArgumentsForParameters(DotNetGenericParameter[] methodGenericParameters,
															Map<DotNetGenericParameter, DotNetTypeRef> map,
															DotNetTypeResolveResult parameterTypeResolveResult,
															DotNetGenericExtractor expressionExtractor,
															DotNetGenericParameter[] expressionGenericParameters)
	{
		for(DotNetGenericParameter methodGenericParameter : methodGenericParameters)
		{
			if(map.containsKey(methodGenericParameter))
			{
				continue;
			}

			ProgressManager.checkCanceled();

			int indexOfGeneric = findIndexOfGeneric(parameterTypeResolveResult, methodGenericParameter);
			if(indexOfGeneric != -1)
			{
				DotNetGenericParameter genericParameterOfResolved = ArrayUtil2.safeGet(expressionGenericParameters, indexOfGeneric);
				if(genericParameterOfResolved == null)
				{
					continue;
				}

				DotNetTypeRef extract = expressionExtractor.extract(genericParameterOfResolved);
				if(extract != null)
				{
					map.put(methodGenericParameter, extract);
				}
			}
		}
	}

	@RequiredReadAction
	private static void pushTypeArgumentsDeep(@Nonnull List<Couple<DotNetTypeResolveResult>> levels,
											  @Nonnull DotNetGenericParameter[] expressionGenericParameters,
											  @Nonnull DotNetGenericExtractor expressionExtractor,
											  @Nonnull DotNetGenericExtractor parameterExtractor)
	{
		for(DotNetGenericParameter expressionGenericParameter : expressionGenericParameters)
		{
			ProgressManager.checkCanceled();

			DotNetTypeRef expressionTypeRefFromGenericParameter = expressionExtractor.extract(expressionGenericParameter);

			DotNetTypeRef parameterTypeRefFromGenericParameter = parameterExtractor.extract(expressionGenericParameter);

			if(expressionTypeRefFromGenericParameter == null || parameterTypeRefFromGenericParameter == null)
			{
				continue;
			}

			DotNetTypeResolveResult exprTypeResult = expressionTypeRefFromGenericParameter.resolve();
			DotNetTypeResolveResult paramTypeResult = parameterTypeRefFromGenericParameter.resolve();

			// check is type equal 'Genre<System.String>' to 'Genre<T>'
			if(exprTypeResult.getElement() instanceof DotNetTypeDeclaration && exprTypeResult.getElement().isEquivalentTo(paramTypeResult.getElement()))
			{
				levels.add(Couple.of(exprTypeResult, paramTypeResult));

				DotNetGenericParameter[] genericParameters = ((DotNetTypeDeclaration) exprTypeResult.getElement()).getGenericParameters();
				pushTypeArgumentsDeep(levels, genericParameters, exprTypeResult.getGenericExtractor(), paramTypeResult.getGenericExtractor());
			}
		}
	}

	@RequiredReadAction
	private static int findIndexOfGeneric(DotNetTypeResolveResult parameterTypeResolveResult, DotNetGenericParameter methodGenericParameter)
	{
		PsiElement parameterElement = parameterTypeResolveResult.getElement();
		if(parameterElement instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) parameterElement).getGenericParameters();
			if(genericParameters.length == 0)
			{
				return -1;
			}

			DotNetGenericExtractor genericExtractor = parameterTypeResolveResult.getGenericExtractor();

			for(int i = 0; i < genericParameters.length; i++)
			{
				DotNetTypeRef extractedTypeRef = genericExtractor.extract(genericParameters[i]);
				if(extractedTypeRef == null)
				{
					continue;
				}
				DotNetTypeResolveResult extractedTypeResolveResult = extractedTypeRef.resolve();
				if(methodGenericParameter.isEquivalentTo(extractedTypeResolveResult.getElement()))
				{
					return i;
				}
			}
		}
		return -1;
	}

	@Nonnull
	@RequiredReadAction
	private static DotNetTypeRef unwrapPossibleGenericTypeRefs(@Nonnull NCallArgument nCallArgument,
															   @Nonnull DotNetTypeRef parameterTypeRef,
															   @Nonnull Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		DotNetTypeRef expressionTypeRef = nCallArgument.getTypeRef();

		CSharpCallArgument callArgument = nCallArgument.getCallArgument();
		if(callArgument == null)
		{
			return expressionTypeRef;
		}

		DotNetExpression argumentExpression = callArgument.getArgumentExpression();
		if(!(argumentExpression instanceof CSharpLambdaExpressionImpl))
		{
			return expressionTypeRef;
		}

		CSharpLambdaExpressionImpl lambdaExpression = (CSharpLambdaExpressionImpl) argumentExpression;

		CSharpLambdaTypeRef baseTypeRefOfLambda = new CSharpLambdaTypeRef(lambdaExpression.getProject(), lambdaExpression.getResolveScope(), null, lambdaExpression.getParameterInfos(), DotNetTypeRef
				.AUTO_TYPE);
		if(CSharpTypeUtil.isInheritable(parameterTypeRef, baseTypeRefOfLambda))
		{
			DotNetTypeResolveResult parameterTypeResult = parameterTypeRef.resolve();
			if(!(parameterTypeResult instanceof CSharpLambdaResolveResult))
			{
				return expressionTypeRef;
			}

			CSharpMethodDeclaration target = ((CSharpLambdaResolveResult) parameterTypeResult).getTarget();

			assert target != null;

			DotNetGenericExtractor extractor = CSharpGenericExtractor.create(map);

			CSharpMethodDeclaration extractedMethod = GenericUnwrapTool.extract(target, extractor);

			GenericInferenceManager inferenceManager = GenericInferenceManager.getInstance(lambdaExpression.getProject());

			return inferenceManager.doWithSession(lambdaExpression, inferenceSessionData ->
			{
				inferenceSessionData.append(lambdaExpression, GenericUnwrapTool.exchangeTypeRef(new CSharpLambdaTypeRef(extractedMethod), extractor));

				return lambdaExpression.toTypeRefForInference();
			});
		}

		return expressionTypeRef;
	}
}
