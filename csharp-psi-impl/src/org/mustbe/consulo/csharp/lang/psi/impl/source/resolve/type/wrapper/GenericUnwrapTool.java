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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDefStatement;
import org.mustbe.consulo.csharp.lang.psi.impl.light.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpErrorTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpReferenceTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.intellij.util.ObjectUtil;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public class GenericUnwrapTool
{
	public static class GenericExtractFunction implements Function<PsiElement, DotNetTypeRef>
	{
		private DotNetGenericExtractor myExtractor;

		public GenericExtractFunction(DotNetGenericExtractor extractor)
		{
			myExtractor = extractor;
		}

		@Override
		public DotNetTypeRef fun(PsiElement element)
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
	}

	public static class TypeDefCleanFunction implements Function<PsiElement, DotNetTypeRef>
	{
		public static final Function<PsiElement, DotNetTypeRef> INSTANCE = new TypeDefCleanFunction();

		@Override
		public DotNetTypeRef fun(PsiElement element)
		{
			if(element instanceof CSharpTypeDefStatement)
			{
				return ((CSharpTypeDefStatement) element).toTypeRef();
			}
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends DotNetNamedElement> T extract(T namedElement, DotNetGenericExtractor extractor)
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
			return (T) copy;
		}
		else if(namedElement instanceof CSharpArrayMethodDeclaration)
		{
			CSharpArrayMethodDeclaration arrayMethodDeclaration = (CSharpArrayMethodDeclaration) namedElement;

			DotNetParameterList parameterList = arrayMethodDeclaration.getParameterList();

			DotNetParameter[] parameters = arrayMethodDeclaration.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRef(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			CSharpLightArrayMethodDeclaration copy = new CSharpLightArrayMethodDeclaration(arrayMethodDeclaration, parameterList);
			exchangeMethodTypeRefs(copy, arrayMethodDeclaration, extractor);
			return (T) copy;
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
			CSharpLightConversionMethodDeclaration copy = new CSharpLightConversionMethodDeclaration(conversionMethodDeclaration, parameterList,
					returnTypeRef);
			return (T) copy;
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
			return (T) copy;
		}
		else if(namedElement instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration e = (CSharpPropertyDeclaration) namedElement;
			DotNetTypeRef returnTypeRef = exchangeTypeRef(e.toTypeRef(true), extractor, e);
			DotNetTypeRef virtualTypeForImpl = exchangeTypeRef(e.getTypeRefForImplement(), extractor, e);
			return (T) new CSharpLightPropertyDeclaration(e, returnTypeRef, virtualTypeForImpl);
		}
		else if(namedElement instanceof CSharpEventDeclaration)
		{
			CSharpEventDeclaration e = (CSharpEventDeclaration) namedElement;
			DotNetTypeRef returnTypeRef = exchangeTypeRef(e.toTypeRef(true), extractor, e);
			DotNetTypeRef virtualTypeForImpl = exchangeTypeRef(e.getTypeRefForImplement(), extractor, e);
			return (T) new CSharpLightEventDeclaration(e, returnTypeRef, virtualTypeForImpl);
		}
		else if(namedElement instanceof CSharpFieldDeclaration)
		{
			CSharpFieldDeclaration e = (CSharpFieldDeclaration) namedElement;
			return (T) new CSharpLightFieldDeclaration(e, exchangeTypeRef(e.toTypeRef(true), extractor, e));
		}

		return namedElement;
	}

	private static <S extends DotNetLikeMethodDeclaration & DotNetVirtualImplementOwner> void exchangeMethodTypeRefs(
			CSharpLightLikeMethodDeclarationWithImplType<?> copy,
			S original,
			DotNetGenericExtractor extractor)
	{
		copy.withReturnTypeRef(exchangeTypeRef(original.getReturnTypeRef(), extractor, original));

		copy.withTypeRefForImplement(exchangeTypeRef(original.getTypeRefForImplement(), extractor, original));
	}

	@NotNull
	public static DotNetTypeRef[] exchangeTypeRefs(DotNetTypeRef[] typeRefs, DotNetGenericExtractor extractor, PsiElement element)
	{
		return exchangeTypeRefs(typeRefs, new GenericExtractFunction(extractor), element);
	}

	@NotNull
	public static DotNetTypeRef exchangeTypeRef(@NotNull DotNetTypeRef typeRef, @NotNull DotNetGenericExtractor extractor, @NotNull PsiElement scope)
	{
		return exchangeTypeRef(typeRef, new GenericExtractFunction(extractor), scope);
	}

	@NotNull
	public static DotNetTypeRef[] exchangeTypeRefs(@NotNull DotNetTypeRef[] typeRefs,
			@NotNull Function<PsiElement, DotNetTypeRef> func,
			@NotNull PsiElement element)
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

	@NotNull
	public static DotNetTypeRef exchangeTypeRef(@NotNull DotNetTypeRef typeRef,
			@NotNull Function<PsiElement, DotNetTypeRef> func,
			@NotNull PsiElement scope)
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
			return new CSharpLazyGenericWrapperTypeRef(scope, inner, arguments);
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			return new CSharpPointerTypeRef(exchangeTypeRef(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), func, scope));
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			return new CSharpRefTypeRef(((CSharpRefTypeRef) typeRef).getType(), exchangeTypeRef(((CSharpRefTypeRef) typeRef).getInnerTypeRef(),
					func, scope));
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			CSharpArrayTypeRef arrayType = (CSharpArrayTypeRef) typeRef;
			return new CSharpArrayTypeRef(exchangeTypeRef(arrayType.getInnerTypeRef(), func, scope), arrayType.getDimensions());
		}
		else if(typeRef instanceof CSharpReferenceTypeRef)
		{
			CSharpReferenceExpression referenceExpression = ((CSharpReferenceTypeRef) typeRef).getReferenceExpression();
			DotNetTypeRef[] typeArgumentListRefs = referenceExpression.getTypeArgumentListRefs();

			Pair<DotNetTypeRef, PsiElement> pair = extractTypeRef(typeRef, func, scope);

			if(typeArgumentListRefs.length == 0)
			{
				return ObjectUtil.notNull(pair.getFirst(), typeRef);
			}

			DotNetTypeRef innerTypeRef;
			if(pair.getFirst() == null && pair.getSecond() == null)
			{
				innerTypeRef = new CSharpErrorTypeRef(referenceExpression.getReferenceName());
			}
			else if(pair.getFirst() != null)
			{
				innerTypeRef = pair.getFirst();
			}
			else // element is not null
			{
				innerTypeRef = CSharpReferenceExpressionImplUtil.toTypeRef(pair.getSecond());
			}

			DotNetTypeRef[] typeRefs = exchangeTypeRefs(typeArgumentListRefs, func, scope);
			return new CSharpLazyGenericWrapperTypeRef(scope, innerTypeRef, typeRefs);
		}
		else
		{
			Pair<DotNetTypeRef, PsiElement> pair = extractTypeRef(typeRef, func, scope);
			if(pair.getFirst() != null)
			{
				return pair.getFirst();
			}
		}
		return typeRef;
	}

	@NotNull
	private static Pair<DotNetTypeRef, PsiElement> extractTypeRef(@NotNull DotNetTypeRef typeRef,
			@NotNull Function<PsiElement, DotNetTypeRef> func,
			@NotNull PsiElement scope)
	{
		PsiElement resolve = typeRef.resolve(scope).getElement();

		DotNetTypeRef extractedTypeRef = func.fun(resolve);
		if(extractedTypeRef != null)
		{
			return Pair.create(extractedTypeRef, resolve);
		}
		return Pair.create(null, resolve);
	}
}
