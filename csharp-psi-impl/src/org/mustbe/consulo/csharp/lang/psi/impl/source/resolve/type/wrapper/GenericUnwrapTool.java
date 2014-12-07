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
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightLikeMethodDeclarationWithImplType;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameterList;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightPropertyDeclaration;
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
import com.intellij.util.ObjectUtils;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public class GenericUnwrapTool
{
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
			return (T) new CSharpLightPropertyDeclaration(e, exchangeTypeRef(e.toTypeRef(true), extractor, e));
		}
		else if(namedElement instanceof CSharpEventDeclaration)
		{
			CSharpEventDeclaration e = (CSharpEventDeclaration) namedElement;
			return (T) new CSharpLightEventDeclaration(e, exchangeTypeRef(e.toTypeRef(true), extractor, e));
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
		DotNetTypeRef[] newTypeRefs = new DotNetTypeRef[typeRefs.length];
		for(int i = 0; i < typeRefs.length; i++)
		{
			DotNetTypeRef typeRef = typeRefs[i];
			newTypeRefs[i] = exchangeTypeRef(typeRef, extractor, element);
		}
		return newTypeRefs;
	}

	@NotNull
	public static DotNetTypeRef exchangeTypeRef(DotNetTypeRef typeRef, DotNetGenericExtractor extractor, PsiElement scope)
	{
		if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetGenericWrapperTypeRef wrapperTypeRef = (DotNetGenericWrapperTypeRef) typeRef;

			DotNetTypeRef inner = exchangeTypeRef(wrapperTypeRef.getInnerTypeRef(), extractor, scope);
			DotNetTypeRef[] oldArguments = wrapperTypeRef.getArgumentTypeRefs();
			DotNetTypeRef[] arguments = new DotNetTypeRef[oldArguments.length];
			for(int i = 0; i < oldArguments.length; i++)
			{
				DotNetTypeRef oldArgument = oldArguments[i];
				arguments[i] = exchangeTypeRef(oldArgument, extractor, scope);
			}
			return new CSharpLazyGenericWrapperTypeRef(scope, inner, arguments);
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			return new CSharpPointerTypeRef(exchangeTypeRef(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), extractor, scope));
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			return new CSharpRefTypeRef(((CSharpRefTypeRef) typeRef).getType(), exchangeTypeRef(((CSharpRefTypeRef) typeRef).getInnerTypeRef(),
					extractor, scope));
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			CSharpArrayTypeRef arrayType = (CSharpArrayTypeRef) typeRef;
			return new CSharpArrayTypeRef(exchangeTypeRef(arrayType.getInnerTypeRef(), extractor, scope), arrayType.getDimensions());
		}
		else if(typeRef instanceof CSharpReferenceTypeRef)
		{
			CSharpReferenceExpression referenceExpression = ((CSharpReferenceTypeRef) typeRef).getReferenceExpression();
			DotNetTypeRef[] typeArgumentListRefs = referenceExpression.getTypeArgumentListRefs();

			Pair<DotNetTypeRef, PsiElement> pair = extractTypeRef(typeRef, extractor, scope);

			if(typeArgumentListRefs.length == 0)
			{
				return ObjectUtils.notNull(pair.getFirst(), typeRef);
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

			DotNetTypeRef[] typeRefs = exchangeTypeRefs(typeArgumentListRefs, extractor, scope);
			return new CSharpLazyGenericWrapperTypeRef(scope, innerTypeRef, typeRefs);
		}
		else
		{
			Pair<DotNetTypeRef, PsiElement> pair = extractTypeRef(typeRef, extractor, scope);
			if(pair.getFirst() != null)
			{
				return pair.getFirst();
			}
		}
		return typeRef;
	}

	@NotNull
	private static Pair<DotNetTypeRef, PsiElement> extractTypeRef(DotNetTypeRef typeRef, DotNetGenericExtractor extractor, PsiElement scope)
	{
		PsiElement resolve = typeRef.resolve(scope).getElement();
		if(resolve instanceof DotNetGenericParameter)
		{
			DotNetTypeRef extractedTypeRef = extractor.extract((DotNetGenericParameter) resolve);
			if(extractedTypeRef != null)
			{
				return Pair.create(extractedTypeRef, resolve);
			}
		}
		return Pair.create(null, resolve);
	}
}
