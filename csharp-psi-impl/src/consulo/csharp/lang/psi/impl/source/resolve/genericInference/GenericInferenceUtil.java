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

package consulo.csharp.lang.psi.impl.source.resolve.genericInference;

import gnu.trove.THashMap;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpFastImplicitTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.util.ArrayUtil2;

/**
 * @author VISTALL
 * @since 29.10.14
 */
public class GenericInferenceUtil
{
	public static final Key<GenericInferenceUtil.GenericInferenceResult> INFERENCE_RESULT = Key.create("inference.result");

	public static class GenericInferenceResult
	{
		private boolean mySuccess;
		private DotNetGenericExtractor myExtractor;

		public GenericInferenceResult(boolean success, @NotNull DotNetGenericExtractor extractor)
		{
			mySuccess = success;
			myExtractor = extractor;
		}

		public boolean isSuccess()
		{
			return mySuccess;
		}

		@NotNull
		public DotNetGenericExtractor getExtractor()
		{
			return myExtractor;
		}
	}

	@NotNull
	@RequiredReadAction
	public static GenericInferenceResult inferenceGenericExtractor(@NotNull PsiElement referenceElement,
			@NotNull CSharpCallArgumentListOwner callArgumentListOwner,
			@NotNull DotNetLikeMethodDeclaration methodDeclaration)
	{
		CSharpCallArgument[] arguments = callArgumentListOwner.getCallArguments();

		DotNetTypeRef[] typeArgumentListRef = DotNetTypeRef.EMPTY_ARRAY;
		if(referenceElement instanceof CSharpReferenceExpression)
		{
			typeArgumentListRef = ((CSharpReferenceExpression) referenceElement).getTypeArgumentListRefs();
		}
		return inferenceGenericExtractor(arguments, typeArgumentListRef, callArgumentListOwner, methodDeclaration);
	}

	@NotNull
	@RequiredReadAction
	public static GenericInferenceResult inferenceGenericExtractor(@NotNull CSharpCallArgument[] callArguments,
			@NotNull DotNetTypeRef[] typeArgumentListRefs,
			@NotNull PsiElement scope,
			@NotNull DotNetLikeMethodDeclaration methodDeclaration)
	{
		DotNetGenericParameter[] genericParameters = methodDeclaration.getGenericParameters();
		if(genericParameters.length == 0 || typeArgumentListRefs.length > 0)
		{
			DotNetGenericExtractor extractor = genericParameters.length != typeArgumentListRefs.length ? DotNetGenericExtractor.EMPTY : CSharpGenericExtractor.create(genericParameters,
					typeArgumentListRefs);
			return new GenericInferenceResult(genericParameters.length == typeArgumentListRefs.length, extractor);
		}

		List<NCallArgument> methodCallArguments = MethodResolver.buildCallArguments(callArguments, methodDeclaration, scope);

		if(methodCallArguments.isEmpty())
		{
			return new GenericInferenceResult(true, DotNetGenericExtractor.EMPTY);
		}

		final Map<DotNetGenericParameter, DotNetTypeRef> map = new THashMap<>();

		for(NCallArgument nCallArgument : methodCallArguments)
		{
			DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
			if(parameterTypeRef == null)
			{
				continue;
			}

			DotNetTypeRef expressionTypeRef = unwrapPossibleGenericTypeRefs(nCallArgument, parameterTypeRef, map, scope);
			if(expressionTypeRef instanceof CSharpFastImplicitTypeRef)
			{
				DotNetTypeRef mirror = ((CSharpFastImplicitTypeRef) expressionTypeRef).doMirror(parameterTypeRef, scope);
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

						inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pParameterTypeRef, eParameterTypeRef, scope);
					}
				}

				inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pLambdaResolveResult.getReturnTypeRef(), eLambdaResolveResult.getReturnTypeRef(), scope);
			}

			inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, parameterTypeRef, expressionTypeRef, scope);
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

		DotNetTypeResolveResult expressionResultResult = CSharpTypeUtil.findTypeRefFromExtends(expressionTypeRef, parameterTypeRef, scope);
		if(expressionResultResult == null)
		{
			return;
		}

		PsiElement expressionElement = expressionResultResult.getElement();
		DotNetGenericExtractor expressionExtractor = expressionResultResult.getGenericExtractor();

		if(expressionElement instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] expressionGenericParameters = ((DotNetGenericParameterListOwner) expressionElement).getGenericParameters();

			for(DotNetGenericParameter methodGenericParameter : methodGenericParameters)
			{
				if(map.containsKey(methodGenericParameter))
				{
					continue;
				}

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

	@NotNull
	@RequiredReadAction
	private static DotNetTypeRef unwrapPossibleGenericTypeRefs(@NotNull NCallArgument nCallArgument,
			@NotNull DotNetTypeRef parameterTypeRef,
			@NotNull Map<DotNetGenericParameter, DotNetTypeRef> map,
			@NotNull PsiElement scope)
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

		CSharpLambdaTypeRef baseTypeRefOfLambda = new CSharpLambdaTypeRef(scope, null, ((CSharpLambdaExpressionImpl) argumentExpression).getParameterInfos(), DotNetTypeRef.AUTO_TYPE);
		if(CSharpTypeUtil.isInheritable(parameterTypeRef, baseTypeRefOfLambda, scope))
		{
			//TODO [VISTALL] find another way to duplicate expression
			final PsiFile fileCopy = (PsiFile) argumentExpression.getContainingFile().copy();

			PsiElement elementAt = fileCopy.findElementAt(argumentExpression.getTextOffset());
			CSharpLambdaExpressionImpl copy = PsiTreeUtil.getParentOfType(elementAt, CSharpLambdaExpressionImpl.class);

			assert copy != null;

			DotNetGenericExtractor extractor = CSharpGenericExtractor.create(map);
			DotNetTypeRef newParameterTypeRef = GenericUnwrapTool.exchangeTypeRef(parameterTypeRef, extractor, scope);
			copy.putUserData(CSharpLambdaExpressionImplUtil.TYPE_REF_OF_LAMBDA, newParameterTypeRef);

			return copy.toTypeRefForInference();
		}

		return expressionTypeRef;
	}
}
