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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetPointerTypeRefImpl;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetRefTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilModifierElementType;
import org.mustbe.consulo.msil.lang.psi.MsilModifierList;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilNativeTypeRefImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class MsilToCSharpUtil
{
	private static Map<MsilEntry, PsiElement> ourCache = new ConcurrentHashMap<MsilEntry, PsiElement>();

	public static boolean hasCSharpInMsilModifierList(CSharpModifier modifier, MsilModifierList modifierList)
	{
		MsilModifierElementType elementType = null;
		switch(modifier)
		{
			case PUBLIC:
				elementType = MsilTokens.PUBLIC_KEYWORD;
				break;
			case PRIVATE:
				elementType = MsilTokens.PRIVATE_KEYWORD;
				break;
			case PROTECTED:
				elementType = MsilTokens.PROTECTED_KEYWORD;
				break;
			case STATIC:
				elementType = MsilTokens.STATIC_KEYWORD;
				break;
			case SEALED:
				elementType = MsilTokens.SEALED_KEYWORD;
				break;
			case INTERNAL:
				elementType = MsilTokens.ASSEMBLY_KEYWORD;
				break;
			case OUT:
				elementType = MsilTokens.BRACKET_OUT_KEYWORD;
				break;
			case VIRTUAL:
				elementType = MsilTokens.VIRTUAL_KEYWORD;
				break;
			case READONLY:
				elementType = MsilTokens.INITONLY_KEYWORD;
				break;
			case UNSAFE:
				break;
			case PARAMS:
				return hasAttribute(modifierList, DotNetTypes.System.ParamArrayAttribute);
			case THIS:
				return hasAttribute(modifierList, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute);
			case ABSTRACT:
				elementType = MsilTokens.ABSTRACT_KEYWORD;
				break;
		}
		return elementType != null && modifierList.hasModifier(elementType);
	}

	private static boolean hasAttribute(MsilModifierList modifierList, String qName)
	{
		for(DotNetAttribute attribute : modifierList.getAttributes())
		{
			DotNetTypeDeclaration typeDeclaration = attribute.resolveToType();
			if(typeDeclaration != null && Comparing.equal(typeDeclaration.getPresentableQName(), qName))
			{
				return true;
			}
		}
		return false;
	}

	@Nullable
	public static PsiElement wrap(PsiElement element)
	{
		if(element instanceof MsilClassEntry)
		{
			PsiElement cache = ourCache.get(element);
			if(cache != null)
			{
				return cache;
			}

			cache = wrapToDelegateMethod((DotNetTypeDeclaration) element);
			if(cache == null)
			{
				cache = new MsilClassAsCSharpTypeDefinition(null, (MsilClassEntry) element);
			}
			ourCache.put((MsilClassEntry) element, cache);
			return cache;
		}
		return element;
	}

	@Nullable
	public static CSharpMethodDeclaration wrapToDelegateMethod(@NotNull DotNetTypeDeclaration typeDeclaration)
	{
		if(DotNetInheritUtil.isInheritor(typeDeclaration, DotNetTypes.System.MulticastDelegate, true))
		{
			val msilMethodEntry = (MsilMethodEntry) ContainerUtil.find((typeDeclaration).getMembers(), new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement element)
				{
					return element instanceof MsilMethodEntry && Comparing.equal(element.getName(), "Invoke");
				}
			});

			assert msilMethodEntry != null : typeDeclaration.getPresentableQName();

			return new MsilMethodAsCSharpMethodDeclaration(null, typeDeclaration, msilMethodEntry);
		}
		else
		{
			return null;
		}
	}

	public static DotNetTypeRef extractToCSharp(DotNetTypeRef typeRef, PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(typeRef instanceof MsilNativeTypeRefImpl)
		{
			String qualifiedText = typeRef.getQualifiedText();
			boolean nullable = false;
			if(DotNetTypes.System.Object.equals(qualifiedText) || DotNetTypes.System.String.equals(qualifiedText))
			{
				nullable = true;
			}
			return new DotNetTypeRefByQName(typeRef.getQualifiedText(), CSharpTransform.INSTANCE, nullable);
		}
		else if(typeRef instanceof MsilArrayTypRefImpl)
		{
			return new CSharpArrayTypeRef(extractToCSharp(((MsilArrayTypRefImpl) typeRef).getInnerTypeRef(), scope), 0);
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			return new DotNetPointerTypeRefImpl(extractToCSharp(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof DotNetRefTypeRef)
		{
			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.ref, extractToCSharp(((DotNetRefTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			val inner = extractToCSharp(((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef(), scope);
			DotNetTypeRef[] arguments = ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs();
			DotNetTypeRef[] newArguments = new DotNetTypeRef[arguments.length];
			for(int i = 0; i < newArguments.length; i++)
			{
				newArguments[i] = extractToCSharp(arguments[i], scope);
			}
			return new DotNetGenericWrapperTypeRef(inner, newArguments);
		}

		PsiElement resolve = typeRef.resolve(scope);
		if(resolve instanceof DotNetTypeDeclaration)
		{
			CSharpMethodDeclaration delegateMethod = wrapToDelegateMethod((DotNetTypeDeclaration) resolve);
			if(delegateMethod != null)
			{
				return new CSharpLambdaTypeRef(delegateMethod);
			}
		}
		return new MsilDelegateTypeRef(typeRef);
	}
}
