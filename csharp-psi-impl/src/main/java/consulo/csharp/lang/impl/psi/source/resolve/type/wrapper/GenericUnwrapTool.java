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

package consulo.csharp.lang.impl.psi.source.resolve.type.wrapper;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.light.*;
import consulo.csharp.lang.impl.psi.source.resolve.type.*;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.*;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.ObjectUtil;
import consulo.util.lang.lazy.LazyValue;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

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

		if(namedElement instanceof CSharpMethodDeclaration methodDeclaration)
		{
			DotNetParameterList parameterList = methodDeclaration.getParameterList();

			DotNetParameter[] parameters = methodDeclaration.getParameters();
			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, LazyValue.notNull(() -> exchangeTypeRef(parameter.toTypeRef(true), extractor)));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightMethodDeclaration copy = new CSharpLightMethodDeclaration(methodDeclaration, parameterList);
			exchangeMethodTypeRefs(copy, methodDeclaration, extractor);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpTypeDeclaration typeDeclaration)
		{
			if(parent == null && typeDeclaration.getGenericParametersCount() == 0)
			{
				return namedElement;
			}

			if(namedElement instanceof CSharpLightTypeDeclaration lightTypeDeclaration && lightTypeDeclaration.getExtractor().equals(extractor))
			{
				return namedElement;
			}

			return cast(new CSharpLightTypeDeclaration(typeDeclaration, extractor), parent);
		}
		else if(namedElement instanceof CSharpIndexMethodDeclaration arrayMethodDeclaration)
		{
			DotNetParameterList parameterList = arrayMethodDeclaration.getParameterList();

			DotNetParameter[] parameters = arrayMethodDeclaration.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, LazyValue.notNull(() -> exchangeTypeRef(parameter.toTypeRef(true), extractor)));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightIndexMethodDeclaration copy = new CSharpLightIndexMethodDeclaration(arrayMethodDeclaration, parameterList);
			exchangeMethodTypeRefs(copy, arrayMethodDeclaration, extractor);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpConversionMethodDeclaration conversionMethodDeclaration)
		{
			DotNetParameterList parameterList = conversionMethodDeclaration.getParameterList();

			DotNetParameter[] parameters = conversionMethodDeclaration.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, LazyValue.notNull(() -> exchangeTypeRef(parameter.toTypeRef(true), extractor)));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			DotNetTypeRef returnTypeRef = exchangeTypeRef(conversionMethodDeclaration.getReturnTypeRef(), extractor);
			CSharpLightConversionMethodDeclaration copy = new CSharpLightConversionMethodDeclaration(conversionMethodDeclaration, parameterList, returnTypeRef, extractor);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpConstructorDeclaration constructor)
		{
			DotNetParameterList parameterList = constructor.getParameterList();

			DotNetParameter[] parameters = constructor.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, LazyValue.notNull(() -> exchangeTypeRef(parameter.toTypeRef(true), extractor)));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightConstructorDeclaration copy = new CSharpLightConstructorDeclaration(constructor, parameterList);
			return cast(copy, parent);
		}
		else if(namedElement instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration e = (CSharpPropertyDeclaration) namedElement;
			Supplier<DotNetTypeRef> returnTypeRef = LazyValue.notNull(() -> exchangeTypeRef(e.toTypeRef(true), extractor));
			Supplier<DotNetTypeRef> virtualTypeForImpl = LazyValue.notNull(() -> exchangeTypeRef(e.getTypeRefForImplement(), extractor));
			return cast(new CSharpLightPropertyDeclaration(e, returnTypeRef, virtualTypeForImpl), parent);
		}
		else if(namedElement instanceof CSharpEventDeclaration)
		{
			CSharpEventDeclaration e = (CSharpEventDeclaration) namedElement;
			DotNetTypeRef returnTypeRef = exchangeTypeRef(e.toTypeRef(true), extractor);
			DotNetTypeRef virtualTypeForImpl = exchangeTypeRef(e.getTypeRefForImplement(), extractor);
			return cast(new CSharpLightEventDeclaration(e, returnTypeRef, virtualTypeForImpl), parent);
		}
		else if(namedElement instanceof CSharpFieldDeclaration)
		{
			CSharpFieldDeclaration e = (CSharpFieldDeclaration) namedElement;
			return cast(new CSharpLightFieldDeclaration(e, exchangeTypeRef(e.toTypeRef(true), extractor)), parent);
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
		copy.withReturnTypeRef(LazyValue.notNull(() -> exchangeTypeRef(original.getReturnTypeRef(), extractor)));
		copy.withTypeRefForImplement(LazyValue.notNull(() -> exchangeTypeRef(original.getTypeRefForImplement(), extractor)));
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef[] exchangeTypeRefs(DotNetTypeRef[] typeRefs, DotNetGenericExtractor extractor)
	{
		return exchangeTypeRefs(typeRefs, new GenericExtractFunction(extractor));
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef exchangeTypeRef(@Nonnull DotNetTypeRef typeRef, @Nonnull DotNetGenericExtractor extractor)
	{
		return exchangeTypeRef(typeRef, new GenericExtractFunction(extractor));
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef[] exchangeTypeRefs(@Nonnull DotNetTypeRef[] typeRefs, @Nonnull UnwrapTypeRefProcessor func)
	{
		if(typeRefs.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] newTypeRefs = new DotNetTypeRef[typeRefs.length];
		for(int i = 0; i < typeRefs.length; i++)
		{
			DotNetTypeRef typeRef = typeRefs[i];
			newTypeRefs[i] = exchangeTypeRef(typeRef, func);
		}
		return newTypeRefs;
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef exchangeTypeRef(@Nonnull DotNetTypeRef typeRef, @Nonnull UnwrapTypeRefProcessor func)
	{
		if(typeRef instanceof DotNetTypeRef.AdapterInternal)
		{
			return typeRef;
		}

		Project project = typeRef.getProject();
		GlobalSearchScope resolveScope = typeRef.getResolveScope();

		if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetGenericWrapperTypeRef wrapperTypeRef = (DotNetGenericWrapperTypeRef) typeRef;

			DotNetTypeRef inner = exchangeTypeRef(wrapperTypeRef.getInnerTypeRef(), func);
			DotNetTypeRef[] oldArguments = wrapperTypeRef.getArgumentTypeRefs();
			DotNetTypeRef[] arguments = new DotNetTypeRef[oldArguments.length];
			for(int i = 0; i < oldArguments.length; i++)
			{
				DotNetTypeRef oldArgument = oldArguments[i];
				arguments[i] = exchangeTypeRef(oldArgument, func);
			}
			return new CSharpGenericWrapperTypeRef(project, resolveScope, inner, arguments);
		}
		else if(typeRef instanceof CSharpPointerTypeRef)
		{
			return new CSharpPointerTypeRef(exchangeTypeRef(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), func));
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			return new CSharpRefTypeRef(project, resolveScope, ((CSharpRefTypeRef) typeRef).getType(), exchangeTypeRef(((CSharpRefTypeRef) typeRef).getInnerTypeRef(), func));
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			CSharpArrayTypeRef arrayType = (CSharpArrayTypeRef) typeRef;
			return new CSharpArrayTypeRef(project, resolveScope, exchangeTypeRef(arrayType.getInnerTypeRef(), func), arrayType.getDimensions());
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

					Map<DotNetGenericParameter, DotNetTypeRef> newExtractorMap = new HashMap<>();
					for(DotNetGenericParameter genericParameter : genericParameters)
					{
						DotNetTypeRef extractTypeRef = genericExtractor.extract(genericParameter);
						if(extractTypeRef == null)
						{
							extractTypeRef = new CSharpTypeRefFromGenericParameter(genericParameter);
						}

						DotNetTypeRef unwrapTypeRef = exchangeTypeRef(extractTypeRef, func);
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
