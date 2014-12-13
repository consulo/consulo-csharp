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

import org.jboss.netty.util.internal.ConcurrentWeakKeyHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.DotNetTypes2;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyGenericWrapperTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetRefTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilModifierElementType;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilNativeTypeRefImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class MsilToCSharpUtil
{
	private static Map<MsilEntry, PsiElement> ourCache = new ConcurrentWeakKeyHashMap<MsilEntry, PsiElement>();

	public static boolean hasCSharpInMsilModifierList(CSharpModifier modifier, DotNetModifierList modifierList)
	{
		MsilModifierElementType elementType = null;
		switch(modifier)
		{
			case PUBLIC:
				if(hasModifierInParentIfType(modifierList, MsilTokens.INTERFACE_KEYWORD))
				{
					return false;
				}
				elementType = MsilTokens.PUBLIC_KEYWORD;
				break;
			case PRIVATE:
				elementType = MsilTokens.PRIVATE_KEYWORD;
				break;
			case PROTECTED:
				elementType = MsilTokens.PROTECTED_KEYWORD;
				break;
			case STATIC:
				// all members with extension attribute are static
				if(hasAttribute(modifierList, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute))
				{
					return true;
				}
				elementType = MsilTokens.STATIC_KEYWORD;
				break;
			case SEALED:
				// hide sealed attribute
				if(hasAttribute(modifierList, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute))
				{
					return false;
				}
				elementType = MsilTokens.SEALED_KEYWORD;
				break;
			case ASYNC:
				return hasAttribute(modifierList, DotNetTypes2.System.Runtime.CompilerServices.AsyncStateMachineAttribute);
			case INTERNAL:
				elementType = MsilTokens.ASSEMBLY_KEYWORD;
				break;
			case OUT:
				elementType = MsilTokens.BRACKET_OUT_KEYWORD;
				break;
			case VIRTUAL:
				if(hasModifierInParentIfType(modifierList, MsilTokens.INTERFACE_KEYWORD))
				{
					return false;
				}
				elementType = MsilTokens.VIRTUAL_KEYWORD;
				break;
			case READONLY:
				elementType = MsilTokens.INITONLY_KEYWORD;
				break;
			case UNSAFE:
				break;
			case PARAMS:
				return hasAttribute(modifierList, DotNetTypes.System.ParamArrayAttribute);
			case ABSTRACT:
				// hide abstract attribute
				if(hasAttribute(modifierList, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute))
				{
					return false;
				}
				elementType = MsilTokens.ABSTRACT_KEYWORD;
				break;
		}
		return elementType != null && modifierList.hasModifier(elementType);
	}

	@SuppressWarnings("unchecked")
	private static <T extends DotNetModifierListOwner> boolean hasModifierInParentIfType(DotNetModifierList msilModifierList, DotNetModifier modifier)
	{
		PsiElement parent = msilModifierList.getParent();
		if(parent == null)
		{
			return false;
		}

		PsiElement parent1 = parent.getParent();
		if(!(parent1 instanceof MsilClassEntry))
		{
			return false;
		}

		T modifierListOwner = (T) parent1;
		return modifierListOwner.hasModifier(modifier);
	}

	private static boolean hasAttribute(DotNetModifierList modifierList, String qName)
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

	@NotNull
	public static PsiElement wrap(PsiElement element)
	{
		return wrap(element, null);
	}

	@NotNull
	public static PsiElement wrap(PsiElement element, @Nullable PsiElement parent)
	{
		if(element instanceof MsilClassEntry)
		{
			PsiElement cache = ourCache.get(element);
			if(cache != null)
			{
				return cache;
			}

			cache = wrapToDelegateMethod((DotNetTypeDeclaration) element, parent);
			if(cache == null)
			{
				cache = new MsilClassAsCSharpTypeDefinition(parent, (MsilClassEntry) element);
			}
			ourCache.put((MsilClassEntry) element, cache);
			return cache;
		}
		return element;
	}

	@Nullable
	public static CSharpMethodDeclaration wrapToDelegateMethod(@NotNull DotNetTypeDeclaration typeDeclaration, @Nullable PsiElement parent)
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

			return new MsilMethodAsCSharpMethodDeclaration(parent, typeDeclaration, msilMethodEntry);
		}
		else
		{
			return null;
		}
	}

	@NotNull
	public static DotNetTypeRef extractToCSharp(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		return extractToCSharp(typeRef, scope, null);
	}

	@NotNull
	private static DotNetTypeRef extractToCSharp(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope, @Nullable Boolean forceNullable)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(typeRef instanceof MsilNativeTypeRefImpl)
		{
			return new CSharpLazyTypeRefByQName(scope, typeRef.getQualifiedText(), forceNullable == Boolean.TRUE);
		}
		else if(typeRef instanceof MsilArrayTypRefImpl)
		{
			int[] lowerValues = ((MsilArrayTypRefImpl) typeRef).getLowerValues();
			return new CSharpArrayTypeRef(extractToCSharp(((MsilArrayTypRefImpl) typeRef).getInnerTypeRef(), scope),
					lowerValues.length == 0 ? 0 : lowerValues.length - 1);
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			return new CSharpPointerTypeRef(extractToCSharp(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof DotNetRefTypeRef)
		{
			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.ref, extractToCSharp(((DotNetRefTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetTypeRef innerTypeRef = ((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef();
			DotNetTypeRef[] arguments = ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs();

			val inner = extractToCSharp(innerTypeRef, scope);
			DotNetTypeRef[] newArguments = new DotNetTypeRef[arguments.length];
			for(int i = 0; i < newArguments.length; i++)
			{
				newArguments[i] = extractToCSharp(arguments[i], scope);
			}

			return new CSharpLazyGenericWrapperTypeRef(scope, inner, newArguments);
		}

		PsiElement resolve = typeRef.resolve(scope).getElement();
		if(resolve instanceof DotNetTypeDeclaration)
		{
			CSharpMethodDeclaration delegateMethod = wrapToDelegateMethod((DotNetTypeDeclaration) resolve, null);
			if(delegateMethod != null)
			{
				return new CSharpLazyLambdaTypeRef(scope, delegateMethod);
			}
		}
		else if(resolve instanceof DotNetGenericParameter)
		{
			return new CSharpTypeRefFromGenericParameter(new MsilGenericParameterAsCSharpGenericParameter(null, (DotNetGenericParameter) resolve));
		}
		return new MsilDelegateTypeRef(scope, typeRef, forceNullable);
	}
}
