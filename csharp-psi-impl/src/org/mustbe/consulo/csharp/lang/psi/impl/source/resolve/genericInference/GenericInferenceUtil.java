package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.genericInference;

import gnu.trove.THashMap;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

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
			DotNetGenericExtractor extractor = genericParameters.length != typeArgumentListRefs.length ? DotNetGenericExtractor.EMPTY :
					CSharpGenericExtractor.create(genericParameters, typeArgumentListRefs);
			return new GenericInferenceResult(genericParameters.length == typeArgumentListRefs.length, extractor);
		}

		List<NCallArgument> methodCallArguments = MethodResolver.buildCallArguments(callArguments, methodDeclaration, scope);

		if(methodCallArguments.isEmpty())
		{
			return new GenericInferenceResult(true, DotNetGenericExtractor.EMPTY);
		}

		final Map<DotNetGenericParameter, DotNetTypeRef> map = new THashMap<DotNetGenericParameter, DotNetTypeRef>();

		for(NCallArgument nCallArgument : methodCallArguments)
		{
			DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
			if(parameterTypeRef == null)
			{
				continue;
			}

			DotNetTypeRef expressionTypeRef = unwrapPossibleGenericTypeRefs(nCallArgument, parameterTypeRef, map, scope);

			DotNetTypeResolveResult parameterTypeResolveResult = parameterTypeRef.resolve(scope);
			DotNetTypeResolveResult expressionTypeResolveResult = expressionTypeRef.resolve(scope);

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

						inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pParameterTypeRef, eParameterTypeRef,
								scope);
					}
				}

				inferenceGenericFromExpressionTypeRefAndParameterTypeRef(genericParameters, map, pLambdaResolveResult.getReturnTypeRef(),
						eLambdaResolveResult.getReturnTypeRef(), scope);
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

		DotNetTypeResolveResult parameterTypeResolveResult = parameterTypeRef.resolve(scope);

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

		DotNetTypeResolveResult typeRefFromExtends = CSharpTypeUtil.findTypeRefFromExtends(expressionTypeRef, parameterTypeRef, scope);
		if(typeRefFromExtends == null)
		{
			return;
		}

		PsiElement element = typeRefFromExtends.getElement();
		DotNetGenericExtractor genericExtractor = typeRefFromExtends.getGenericExtractor();

		if(element instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParametersOfResolved = ((DotNetGenericParameterListOwner) element).getGenericParameters();

			for(DotNetGenericParameter genericParameter : methodGenericParameters)
			{
				if(map.containsKey(genericParameter))
				{
					continue;
				}

				int indexOfGeneric = findIndexOfGeneric(parameterTypeResolveResult, genericParameter, scope);
				if(indexOfGeneric == -1)
				{
					continue;
				}

				DotNetGenericParameter genericParameterOfResolved = ArrayUtil2.safeGet(genericParametersOfResolved, indexOfGeneric);
				if(genericParameterOfResolved == null)
				{
					continue;
				}

				DotNetTypeRef extract = genericExtractor.extract(genericParameterOfResolved);
				if(extract != null)
				{
					map.put(genericParameter, extract);
				}
			}
		}
	}

	@RequiredReadAction
	private static int findIndexOfGeneric(DotNetTypeResolveResult parameterTypeResolveResult, DotNetGenericParameter parameter, PsiElement scope)
	{
		PsiElement element = parameterTypeResolveResult.getElement();
		if(element instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) element).getGenericParameters();
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
				DotNetTypeResolveResult extractedTypeResolveResult = extractedTypeRef.resolve(scope);
				if(parameter.isEquivalentTo(extractedTypeResolveResult.getElement()))
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

		CSharpLambdaTypeRef baseTypeRefOfLambda = new CSharpLambdaTypeRef(null, ((CSharpLambdaExpressionImpl) argumentExpression).getParameterInfos
				(), DotNetTypeRef.AUTO_TYPE);
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
