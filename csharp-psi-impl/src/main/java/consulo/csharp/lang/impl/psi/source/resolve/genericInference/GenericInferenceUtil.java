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
import consulo.csharp.lang.impl.psi.CSharpExpressionTreeUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.NCallArgumentBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NParamsCallArgument;
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
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;

import java.util.ArrayList;
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

		public GenericInferenceResult(boolean success, DotNetGenericExtractor extractor)
		{
			mySuccess = success;
			myExtractor = extractor;
		}

		public boolean isSuccess()
		{
			return mySuccess;
		}

		public DotNetGenericExtractor getExtractor()
		{
			return myExtractor;
		}
	}

	@RequiredReadAction
	public static GenericInferenceResult inferenceGenericExtractor(PsiElement referenceElement,
																   CSharpCallArgumentListOwner callArgumentListOwner,
																   DotNetLikeMethodDeclaration methodDeclaration)
	{
		CSharpCallArgument[] arguments = callArgumentListOwner.getCallArguments();

		DotNetTypeRef[] typeArgumentListRef = DotNetTypeRef.EMPTY_ARRAY;
		if(referenceElement instanceof CSharpReferenceExpression)
		{
			typeArgumentListRef = ((CSharpReferenceExpression) referenceElement).getTypeArgumentListRefs();
		}
		return inferenceGenericExtractor(arguments, typeArgumentListRef, callArgumentListOwner, methodDeclaration);
	}

	@RequiredReadAction
	public static GenericInferenceResult inferenceGenericExtractor(CSharpCallArgument[] callArguments,
																   DotNetTypeRef[] typeArgumentListRefs,
																   PsiElement scopeElement,
																   DotNetLikeMethodDeclaration methodDeclaration)
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

		InferenceBounds bounds = new InferenceBounds(genericParameters, scopeElement.getResolveScope());

		// lambdas need the types fixed from the other arguments first (the two phases of C# spec 12.6.3)
		List<NCallArgument> lambdaArguments = new ArrayList<>();
		for(NCallArgument nCallArgument : expandParams(methodCallArguments))
		{
			if(isLambdaArgument(nCallArgument))
			{
				lambdaArguments.add(nCallArgument);
			}
			else
			{
				inferFromArgument(bounds, nCallArgument, Map.of());
			}
		}

		for(NCallArgument lambdaArgument : lambdaArguments)
		{
			inferFromArgument(bounds, lambdaArgument, bounds.fix());
		}

		Map<DotNetGenericParameter, DotNetTypeRef> map = bounds.fix();
		return new GenericInferenceResult(genericParameters.length == map.size(), CSharpGenericExtractor.create(map));
	}

	/**
	 * The return type of the method of a method group called with the parameter types of the delegate - a generic method
	 * infers its own type arguments from them: M71 for Func1&lt;int, R&gt; returns Class&lt;int&gt;.
	 */
	@RequiredReadAction
	private static DotNetTypeRef methodGroupReturnType(NCallArgument nCallArgument,
													   CSharpLambdaResolveResult mirrored,
													   DotNetTypeRef[] delegateParameterTypeRefs,
													   Map<DotNetGenericParameter, DotNetTypeRef> fixedSoFar)
	{
		if(!(nCallArgument.getTypeRef() instanceof CSharpElementGroupTypeRef groupTypeRef))
		{
			return mirrored.getReturnTypeRef();
		}

		DotNetGenericExtractor fixedExtractor = CSharpGenericExtractor.create(fixedSoFar);
		for(PsiElement element : groupTypeRef.getElementGroup().getElements())
		{
			if(!(element instanceof DotNetLikeMethodDeclaration method) || method.getParameterTypeRefs().length != delegateParameterTypeRefs.length)
			{
				continue;
			}

			DotNetGenericParameter[] genericParameters = method.getGenericParameters();
			if(genericParameters.length == 0)
			{
				return method.getReturnTypeRef();
			}

			DotNetTypeRef[] methodParameterTypeRefs = method.getParameterTypeRefs();
			InferenceBounds methodBounds = new InferenceBounds(genericParameters, method.getResolveScope());
			for(int i = 0; i < methodParameterTypeRefs.length; i++)
			{
				methodBounds.inferFromArgument(GenericUnwrapTool.exchangeTypeRef(delegateParameterTypeRefs[i], fixedExtractor), methodParameterTypeRefs[i]);
			}
			return GenericUnwrapTool.exchangeTypeRef(method.getReturnTypeRef(), CSharpGenericExtractor.create(methodBounds.fix()));
		}
		return mirrored.getReturnTypeRef();
	}

	/**
	 * Every argument of a params group against the element type of the params array, so that lambdas in it infer like
	 * any other lambda.
	 */
	@RequiredReadAction
	private static List<NCallArgument> expandParams(List<NCallArgument> arguments)
	{
		List<NCallArgument> result = new ArrayList<>(arguments.size());
		for(NCallArgument argument : arguments)
		{
			DotNetTypeRef parameterTypeRef = argument.getParameterTypeRef();
			if(!(argument instanceof NParamsCallArgument) || parameterTypeRef == null
					|| !(parameterTypeRef.resolve() instanceof CSharpArrayTypeRef.ArrayResolveResult arrayResolveResult))
			{
				result.add(argument);
				continue;
			}

			DotNetTypeRef elementTypeRef = arrayResolveResult.getInnerTypeRef();
			for(CSharpCallArgument callArgument : argument.getCallArguments())
			{
				DotNetExpression expression = callArgument.getArgumentExpression();
				if(expression != null)
				{
					result.add(new NCallArgument(expression.toTypeRef(false), callArgument, elementTypeRef));
				}
			}
		}
		return result;
	}

	/**
	 * A lambda, or a method group passed as a delegate - inferred by the parameters and the return type of the delegate.
	 * A value of the delegate type itself is not one: it infers by its type arguments, like any other generic type.
	 */
	@RequiredReadAction
	private static boolean isLambdaArgument(NCallArgument nCallArgument)
	{
		CSharpCallArgument callArgument = nCallArgument.getCallArgument();
		if(callArgument == null)
		{
			return false;
		}
		// a lambda, or a method group - which becomes a delegate only against the parameter (see CSharpFastImplicitTypeRef)
		if(callArgument.getArgumentExpression() instanceof CSharpLambdaExpressionImpl || nCallArgument.getTypeRef() instanceof CSharpElementGroupTypeRef)
		{
			return true;
		}

		DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
		if(parameterTypeRef == null
				|| !(parameterTypeRef.resolve() instanceof CSharpLambdaResolveResult parameterLambda)
				|| !(nCallArgument.getTypeRef().resolve() instanceof CSharpLambdaResolveResult argumentLambda))
		{
			return false;
		}
		CSharpMethodDeclaration parameterDelegate = parameterLambda.getTarget();
		CSharpMethodDeclaration argumentTarget = argumentLambda.getTarget();
		return parameterDelegate == null || argumentTarget == null || !parameterDelegate.isEquivalentTo(argumentTarget);
	}

	@RequiredReadAction
	private static void inferFromArgument(InferenceBounds bounds, NCallArgument nCallArgument, Map<DotNetGenericParameter, DotNetTypeRef> fixedSoFar)
	{
		ProgressManager.checkCanceled();

		DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
		if(parameterTypeRef == null)
		{
			return;
		}

		DotNetTypeRef expressionTypeRef = unwrapPossibleGenericTypeRefs(nCallArgument, parameterTypeRef, fixedSoFar);
		if(expressionTypeRef instanceof CSharpFastImplicitTypeRef)
		{
			DotNetTypeRef mirror = ((CSharpFastImplicitTypeRef) expressionTypeRef).doMirror(parameterTypeRef);
			if(mirror != null)
			{
				expressionTypeRef = mirror;
			}
		}

		// a lambda or a method group against a delegate type: by the parameters and the return type
		if(isLambdaArgument(nCallArgument)
				&& CSharpExpressionTreeUtil.unwrap(parameterTypeRef).resolve() instanceof CSharpLambdaResolveResult pLambdaResolveResult
				&& expressionTypeRef.resolve() instanceof CSharpLambdaResolveResult eLambdaResolveResult)
		{
			DotNetTypeRef[] pParameterTypeRefs = pLambdaResolveResult.getParameterTypeRefs();
			DotNetTypeRef[] eParameterTypeRefs = eLambdaResolveResult.getParameterTypeRefs();

			// a method group gives its return type only, for the parameter types of the delegate fixed so far
			if(!(nCallArgument.getCallArgument().getArgumentExpression() instanceof CSharpLambdaExpressionImpl))
			{
				DotNetTypeRef returnTypeRef = methodGroupReturnType(nCallArgument, eLambdaResolveResult, pParameterTypeRefs, fixedSoFar);
				bounds.inferLower(returnTypeRef, pLambdaResolveResult.getReturnTypeRef());
				return;
			}

			if(pParameterTypeRefs.length == eParameterTypeRefs.length)
			{
				for(int i = 0; i < eParameterTypeRefs.length; i++)
				{
					bounds.inferExact(eParameterTypeRefs[i], pParameterTypeRefs[i]);
				}
			}

			bounds.inferLower(eLambdaResolveResult.getReturnTypeRef(), pLambdaResolveResult.getReturnTypeRef());
			return;
		}

		bounds.inferFromArgument(expressionTypeRef, parameterTypeRef);
	}

	@RequiredReadAction
	private static DotNetTypeRef unwrapPossibleGenericTypeRefs(NCallArgument nCallArgument,
															   DotNetTypeRef parameterTypeRef,
															   Map<DotNetGenericParameter, DotNetTypeRef> map)
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
