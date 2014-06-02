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
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameterList;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetPointerTypeImpl;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public class GenericUnwrapTool
{
	@SuppressWarnings("unchecked")
	public static <T extends DotNetNamedElement> T extract(T namedElement, DotNetGenericExtractor extractor)
	{
		if(extractor == DotNetGenericExtractor.EMPTY || namedElement instanceof DotNetModifierListOwner && ((DotNetModifierListOwner) namedElement)
				.hasModifier(CSharpModifier.STATIC))
		{
			return namedElement;
		}
		if(namedElement instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) namedElement;

			DotNetTypeRef newReturnTypeRef = exchangeTypeRefs(methodDeclaration.getReturnTypeRef(), extractor, namedElement);

			DotNetParameterList parameterList = methodDeclaration.getParameterList();

			DotNetParameter[] parameters = methodDeclaration.getParameters();
			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRefs(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			return (T) new CSharpLightMethodDeclaration(methodDeclaration, newReturnTypeRef, parameterList);
		}
		else if(namedElement instanceof CSharpArrayMethodDeclaration)
		{
			CSharpArrayMethodDeclaration arrayMethodDeclaration = (CSharpArrayMethodDeclaration) namedElement;

			DotNetTypeRef newReturnTypeRef = exchangeTypeRefs(arrayMethodDeclaration.getReturnTypeRef(), extractor, namedElement);

			DotNetParameterList parameterList = arrayMethodDeclaration.getParameterList();

			DotNetParameter[] parameters = arrayMethodDeclaration.getParameters();

			DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				DotNetParameter parameter = parameters[i];
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRefs(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			return (T) new CSharpLightArrayMethodDeclaration(arrayMethodDeclaration, newReturnTypeRef, parameterList);
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
				newParameters[i] = new CSharpLightParameter(parameter, exchangeTypeRefs(parameter.toTypeRef(true), extractor, parameter));
			}

			parameterList = new CSharpLightParameterList(parameterList == null ? namedElement : parameterList, newParameters);

			return (T) new CSharpLightConstructorDeclaration(constructor, null, parameterList);
		}
		else if(namedElement instanceof CSharpPropertyDeclaration)
		{
			CSharpPropertyDeclaration e = (CSharpPropertyDeclaration) namedElement;
			return (T) new CSharpLightPropertyDeclaration(e, exchangeTypeRefs(e.toTypeRef(true), extractor, e));
		}
		else if(namedElement instanceof CSharpEventDeclaration)
		{
			CSharpEventDeclaration e = (CSharpEventDeclaration) namedElement;
			return (T) new CSharpLightEventDeclaration(e, exchangeTypeRefs(e.toTypeRef(true), extractor, e));
		}
		else if(namedElement instanceof CSharpFieldDeclaration)
		{
			CSharpFieldDeclaration e = (CSharpFieldDeclaration) namedElement;
			return (T) new CSharpLightFieldDeclaration(e, exchangeTypeRefs(e.toTypeRef(true), extractor, e));
		}
		else
		{
			System.out.println("Unsupported: " + namedElement);
		}
		return namedElement;
	}

	@NotNull
	public static DotNetTypeRef exchangeTypeRefs(DotNetTypeRef typeRef, DotNetGenericExtractor extractor, PsiElement element)
	{
		if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetGenericWrapperTypeRef wrapperTypeRef = (DotNetGenericWrapperTypeRef) typeRef;

			DotNetTypeRef inner = exchangeTypeRefs(wrapperTypeRef.getInner(), extractor, element);
			DotNetTypeRef[] oldArguments = wrapperTypeRef.getArguments();
			DotNetTypeRef[] arguments = new DotNetTypeRef[oldArguments.length];
			for(int i = 0; i < oldArguments.length; i++)
			{
				DotNetTypeRef oldArgument = oldArguments[i];
				arguments[i] = exchangeTypeRefs(oldArgument, extractor, element);
			}
			return new DotNetGenericWrapperTypeRef(inner, arguments);
		}
		else if(typeRef instanceof DotNetPointerTypeImpl)
		{
			return new DotNetPointerTypeImpl(exchangeTypeRefs(((DotNetPointerTypeImpl) typeRef).getInnerTypeRef(), extractor, element));
		}
		else if(typeRef instanceof CSharpArrayTypeRef)
		{
			CSharpArrayTypeRef arrayType = (CSharpArrayTypeRef) typeRef;
			return new CSharpArrayTypeRef(exchangeTypeRefs(arrayType.getInnerTypeRef(), extractor, element), arrayType.getDimensions());
		}
		else
		{
			PsiElement resolve = typeRef.resolve(element);
			if(resolve instanceof DotNetGenericParameter)
			{
				DotNetTypeRef extractedTypeRef = extractor.extract((DotNetGenericParameter) resolve);
				if(extractedTypeRef != null)
				{
					return extractedTypeRef;
				}
			}
		}
		return typeRef;
	}
}
