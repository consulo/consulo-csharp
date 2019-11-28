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

package consulo.csharp.lang.psi.impl.source.resolve.type.wrapper;

import gnu.trove.THashMap;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpFieldDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.csharp.lang.psi.impl.light.*;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResultUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.resolve.DotNetPointerTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public class GenericUnwrapTool
{
	protected static interface UnwrapTypeRefProcessor
	{
		@Nullable
		@RequiredReadAction
		DotNetTypeRef unwrap(PsiElement element);

		@Nonnull
		@RequiredReadAction
		default DotNetGenericExtractor getExtractor()
		{
			return DotNetGenericExtractor.EMPTY;
		}
	}

	public static class GenericExtractFunction implements UnwrapTypeRefProcessor
	{
		private DotNetGenericExtractor myExtractor;

		public GenericExtractFunction(DotNetGenericExtractor extractor)
		{
			myExtractor = extractor;
		}

		@RequiredReadAction
		@Override
		public DotNetTypeRef unwrap(final PsiElement element)
		{
			if(element instanceof DotNetGenericParameter)
			{
				DotNetTypeRef extractedTypeRef = myExtractor.extract((DotNetGenericParameter) element);
				if(extractedTypeRef != null)
				{
					return extractedTypeRef;
				}
			}
			return null;
		}

		@RequiredReadAction
		@Nonnull
		@Override
		public DotNetGenericExtractor getExtractor()
		{
			return myExtractor;
		}
	}

	public static class TypeDefCleanFunction implements UnwrapTypeRefProcessor
	{
		public static final UnwrapTypeRefProcessor INSTANCE = new TypeDefCleanFunction();

		@RequiredReadAction
		@Override
		public DotNetTypeRef unwrap(PsiElement element)
		{
			if(element instanceof CSharpTypeDefStatement)
			{
				return ((CSharpTypeDefStatement) element).toTypeRef();
			}
			return null;
		}
	}

	@RequiredReadAction
	public static <T extends DotNetNamedElement> T extract(T namedElement, DotNetGenericExtractor extractor)
	{
		return extract(namedElement, extractor, null);
	}

	@RequiredReadAction
	public static <T extends DotNetNamedElement> T extract(T namedElement, DotNetGenericExtractor extractor, @Nullable PsiElement parent)
	{
		if(extractor == DotNetGenericExtractor.EMPTY)
		{
			return namedElement;
		}

		if(namedElement instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) namedElement;

			DotNetParameterList parameterList = methodDeclaration.getParameterList();

			DotNetParameter[] parameters = methodDeclaration.getParameters();
			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRef(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightMethodDeclaration copy = new CSharpLightMethodDeclaration(methodDeclaration, parameterList);
			exchangeMethodTypeRefs(copy, methodDeclaration, extractor);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpTypeDeclaration)
		{
			return cast(new CSharpLightTypeDeclaration((CSharpTypeDeclaration) namedElement, extractor), parent);
		}
		else if(namedElement instanceof CSharpIndexMethodDeclaration)
		{
			CSharpIndexMethodDeclaration arrayMethodDeclaration = (CSharpIndexMethodDeclaration) namedElement;

			DotNetParameterList parameterList = arrayMethodDeclaration.getParameterList();

			DotNetParameter[] parameters = arrayMethodDeclaration.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRef(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightIndexMethodDeclaration copy = new CSharpLightIndexMethodDeclaration(arrayMethodDeclaration, parameterList);
			exchangeMethodTypeRefs(copy, arrayMethodDeclaration, extractor);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpConversionMethodDeclaration)
		{
			CSharpConversionMethodDeclaration conversionMethodDeclaration = (CSharpConversionMethodDeclaration) namedElement;

			DotNetParameterList parameterList = conversionMethodDeclaration.getParameterList();

			DotNetParameter[] parameters = conversionMethodDeclaration.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRef(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			DotNetTypeRef returnTypeRef = exchangeTypeRef(conversionMethodDeclaration.getReturnTypeRef(), extractor, namedElement);
			CSharpLightConversionMethodDeclaration copy = new CSharpLightConversionMethodDeclaration(conversionMethodDeclaration, parameterList, returnTypeRef);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpConstructorDeclaration)
		{
			CSharpConstructorDeclaration constructor = (CSharpConstructorDeclaration) namedElement;

			DotNetParameterList parameterList = constructor.getParameterList();

			DotNetParameter[] parameters = constructor.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRef(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightConstructorDeclaration copy = new CSharpLightConstructorDeclaration(constructor, parameterList);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration e = (CSharpPropertyDeclaration) namedElement;
			DotNetTypeRef returnTypeRef = exchangeTypeRef(e.toTypeRef(true), extractor, e);
			DotNetTypeRef virtualTypeForImpl = exchangeTypeRef(e.getTypeRefForImplement(), extractor, e);
			return cast(new CSharpLightPropertyDeclaration(e, returnTypeRef, virtualTypeForImpl), parent);
		}
		else if(namedElement instanceof CSharpEventDeclaration)
		{
			CSharpEventDeclaration e = (CSharpEventDeclaration) namedElement;
			DotNetTypeRef returnTypeRef = exchangeTypeRef(e.toTypeRef(true), extractor, e);
			DotNetTypeRef virtualTypeForImpl = exchangeTypeRef(e.getTypeRefForImplement(), extractor, e);
			return cast(new CSharpLightEventDeclaration(e, returnTypeRef, virtualTypeForImpl), parent);
		}
		else if(namedElement instanceof CSharpFieldDeclaration)
		{
			CSharpFieldDeclaration e = (CSharpFieldDeclaration) namedElement;
			return cast(new CSharpLightFieldDeclaration(e, exchangeTypeRef(e.toTypeRef(true), extractor, e)), parent);
		}

		return namedElement;
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	private static <T extends DotNetNamedElement> T cast(@Nonnull PsiElement target, @Nullable PsiElement parent)
	{
		if(parent != null && target instanceof CSharpLightElement)
		{
			return (T) ((CSharpLightElement) target).withParent(parent);
		}
		return (T) target;
	}

	@RequiredReadAction
	private static <S extends DotNetLikeMethodDeclaration & DotNetVirtualImplementOwner> void exchangeMethodTypeRefs(CSharpLightLikeMethodDeclarationWithImplType<?> copy,
			S original,
			DotNetGenericExtractor extractor)
	{
		copy.withReturnTypeRef(exchangeTypeRef(original.getReturnTypeRef(), extractor, original));

		copy.withTypeRefForImplement(exchangeTypeRef(original.getTypeRefForImplement(), extractor, original));
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef[] exchangeTypeRefs(DotNetTypeRef[] typeRefs, DotNetGenericExtractor extractor, PsiElement element)
	{
		return exchangeTypeRefs(typeRefs, new GenericExtractFunction(extractor), element);
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef exchangeTypeRef(@Nonnull DotNetTypeRef typeRef, @Nonnull DotNetGenericExtractor extractor, @Nonnull PsiElement scope)
	{
		return exchangeTypeRef(typeRef, new GenericExtractFunction(extractor), scope);
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef[] exchangeTypeRefs(@Nonnull DotNetTypeRef[] typeRefs, @Nonnull UnwrapTypeRefProcessor func, @Nonnull PsiElement element)
	{
		if(typeRefs.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] newTypeRefs = new DotNetTypeRef[typeRefs.length];
		for(int i = 0; i < typeRefs.length; i++)
		{
			DotNetTypeRef typeRef = typeRefs[i];
			newTypeRefs[i] = exchangeTypeRef(typeRef, func, element);
		}
		return newTypeRefs;
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef exchangeTypeRef(@Nonnull DotNetTypeRef typeRef, @Nonnull UnwrapTypeRefProcessor func, @Nonnull PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetGenericWrapperTypeRef wrapperTypeRef = (DotNetGenericWrapperTypeRef) typeRef;

			DotNetTypeRef inner = exchangeTypeRef(wrapperTypeRef.getInnerTypeRef(), func, scope);
			DotNetTypeRef[] oldArguments = wrapperTypeRef.getArgumentTypeRefs();
			DotNetTypeRef[] arguments = new DotNetTypeRef[oldArguments.length];
			for(int i = 0; i < oldArguments.length; i++)
			{
				DotNetTypeRef oldArgument = oldArguments[i];
				arguments[i] = exchangeTypeRef(oldArgument, func, scope);
			}
			return new CSharpGenericWrapperTypeRef(scope.getProject(), inner, arguments);
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			return new CSharpPointerTypeRef(scope, exchangeTypeRef(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), func, scope));
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			return new CSharpRefTypeRef(scope.getProject(), ((CSharpRefTypeRef) typeRef).getType(), exchangeTypeRef(((CSharpRefTypeRef) typeRef).getInnerTypeRef(), func, scope));
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			CSharpArrayTypeRef arrayType = (CSharpArrayTypeRef) typeRef;
			return new CSharpArrayTypeRef(scope, exchangeTypeRef(arrayType.getInnerTypeRef(), func, scope), arrayType.getDimensions());
		}
		else
		{
			DotNetTypeResolveResult result = typeRef.resolve();

			PsiElement element = result.getElement();

			if(element == null)
			{
				return typeRef;
			}

			if(element instanceof CSharpTypeDeclaration)
			{
				CSharpMethodDeclaration delegateMethodTypeWrapper = CSharpLambdaResolveResultUtil.getDelegateMethodTypeWrapper(element);
				CSharpTypeDeclaration resultType;
				if(delegateMethodTypeWrapper != null)
				{
					resultType = CSharpLambdaResolveResultUtil.createTypeFromDelegate(delegateMethodTypeWrapper, func.getExtractor());
				}
				else
				{
					resultType = extract((CSharpTypeDeclaration) element, func.getExtractor());
				}

				DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) element).getGenericParameters();
				if(genericParameters.length > 0)
				{
					DotNetGenericExtractor genericExtractor = result.getGenericExtractor();

					Map<DotNetGenericParameter, DotNetTypeRef> newExtractorMap = new THashMap<>();
					for(DotNetGenericParameter genericParameter : genericParameters)
					{
						DotNetTypeRef extractTypeRef = genericExtractor.extract(genericParameter);
						if(extractTypeRef == null)
						{
							extractTypeRef = new CSharpTypeRefFromGenericParameter(genericParameter);
						}

						DotNetTypeRef unwrapTypeRef = exchangeTypeRef(extractTypeRef, func, scope);
						newExtractorMap.put(genericParameter, unwrapTypeRef);
					}

					return new CSharpTypeRefByTypeDeclaration(resultType, CSharpGenericExtractor.create(newExtractorMap));
				}

				return new CSharpTypeRefByTypeDeclaration(resultType, DotNetGenericExtractor.EMPTY);
			}
			else
			{
				DotNetTypeRef unwrap = func.unwrap(element);
				return ObjectUtil.notNull(unwrap, typeRef);
			}
		}
	}
}
