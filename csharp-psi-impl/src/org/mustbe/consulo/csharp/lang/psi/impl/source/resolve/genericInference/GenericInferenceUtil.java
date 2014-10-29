package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.genericInference;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamedCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.SimpleGenericExtractorImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 29.10.14
 */
public class GenericInferenceUtil
{
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
	public static GenericInferenceResult inferenceGenericExtractor(@NotNull CSharpCallArgumentListOwner callArgumentListOwner,
			@NotNull DotNetLikeMethodDeclaration methodDeclaration)
	{
		CSharpCallArgumentList parameterList = callArgumentListOwner.getParameterList();
		CSharpCallArgument[] arguments = parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();

		return inferenceGenericExtractor(arguments, callArgumentListOwner.getTypeArgumentListRefs(), callArgumentListOwner, methodDeclaration);
	}

	@NotNull
	public static GenericInferenceResult inferenceGenericExtractor(
			@NotNull CSharpCallArgument[] callArguments,
			@NotNull DotNetTypeRef[] typeArgumentListRefs,
			@NotNull PsiElement scope,
			@NotNull DotNetLikeMethodDeclaration methodDeclaration)
	{
		DotNetGenericParameter[] genericParameters = methodDeclaration.getGenericParameters();
		if(genericParameters.length == 0 || typeArgumentListRefs.length > 0)
		{
			return new GenericInferenceResult(genericParameters.length == typeArgumentListRefs.length,
					new SimpleGenericExtractorImpl(genericParameters, typeArgumentListRefs));
		}

		Map<String, Couple<DotNetTypeRef>> genericInferenceContext = prepareParametersAndArguments(callArguments, methodDeclaration);
		if(genericInferenceContext.isEmpty())
		{
			return new GenericInferenceResult(true, DotNetGenericExtractor.EMPTY);
		}

		val map = new HashMap<DotNetGenericParameter, DotNetTypeRef>();

		for(Couple<DotNetTypeRef> typeRefCouple : genericInferenceContext.values())
		{
			DotNetTypeRef parameterTypeRef = typeRefCouple.getFirst();
			DotNetTypeRef expressionTypeRef = typeRefCouple.getSecond();

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

		return new GenericInferenceResult(genericParameters.length == map.size(), new DotNetGenericExtractor()
		{
			@Nullable
			@Override
			public DotNetTypeRef extract(@NotNull DotNetGenericParameter parameter)
			{
				return map.get(parameter);
			}
		});
	}

	private static void inferenceGenericFromExpressionTypeRefAndParameterTypeRef(DotNetGenericParameter[] methodGenericParameters,
			Map<DotNetGenericParameter, DotNetTypeRef> map,
			DotNetTypeRef parameterTypeRef,
			DotNetTypeRef expressionTypeRef,
			PsiElement scope)
	{
		if(expressionTypeRef == DotNetTypeRef.AUTO_TYPE || expressionTypeRef == DotNetTypeRef.UNKNOWN_TYPE)
		{
			return;
		}

		DotNetTypeResolveResult parameterTypeResolveResult = parameterTypeRef.resolve(scope);

		PsiElement parameterElement = parameterTypeResolveResult.getElement();
		for(DotNetGenericParameter genericParameter : methodGenericParameters)
		{
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
				int indexOfGeneric = findIndexOfGeneric(parameterTypeRef, genericParameter, scope);
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

	private static int findIndexOfGeneric(DotNetTypeRef parameterTypeRef, DotNetGenericParameter parameter, PsiElement scope)
	{
		if(parameterTypeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetTypeRef[] argumentTypeRefs = ((DotNetGenericWrapperTypeRef) parameterTypeRef).getArgumentTypeRefs();

			for(int i = 0; i < argumentTypeRefs.length; i++)
			{
				DotNetTypeRef argumentTypeRef = argumentTypeRefs[i];
				DotNetTypeResolveResult typeResolveResult = argumentTypeRef.resolve(scope);
				if(parameter.isEquivalentTo(typeResolveResult.getElement()))
				{
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * @return map of <parameterName, <ParameterTypeRef, ExpressionTypeRef>>
	 */
	@NotNull
	private static Map<String, Couple<DotNetTypeRef>> prepareParametersAndArguments(@NotNull CSharpCallArgument[] arguments,
			@NotNull DotNetLikeMethodDeclaration methodDeclaration)
	{
		Map<String, Couple<DotNetTypeRef>> types = new LinkedHashMap<String, Couple<DotNetTypeRef>>();

		DotNetParameter[] parameters = methodDeclaration.getParameters();

		int i = 0;
		for(CSharpCallArgument argument : arguments)
		{
			DotNetTypeRef expressionTypeRef = DotNetTypeRef.ERROR_TYPE;
			String name;
			DotNetTypeRef parameterTypeRef;

			DotNetExpression argumentExpression = argument.getArgumentExpression();
			if(argumentExpression != null)
			{
				expressionTypeRef = argumentExpression.toTypeRef(false);
			}

			if(argument instanceof CSharpNamedCallArgument)
			{
				name = ((CSharpNamedCallArgument) argument).getName();

				parameterTypeRef = DotNetTypeRef.ERROR_TYPE;

				for(DotNetParameter parameter : methodDeclaration.getParameters())
				{
					if(Comparing.equal(name, parameter.getName()))
					{
						parameterTypeRef = parameter.toTypeRef(false);
						break;
					}
				}
			}
			else
			{
				DotNetParameter parameter = ArrayUtil2.safeGet(parameters, i++);
				if(parameter == null)
				{
					continue;
				}
				parameterTypeRef = parameter.toTypeRef(false);
				name = parameter.getName();
			}

			if(name == null)
			{
				continue;
			}
			types.put(name, Couple.of(parameterTypeRef, expressionTypeRef));
		}

		return types;
	}
}
