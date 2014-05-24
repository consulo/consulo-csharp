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

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpQualifiedTypeRef;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilModifierElementType;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import org.mustbe.consulo.msil.lang.psi.impl.type.MsilReferenceTypeRefImpl;
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
	@Nullable
	public static MsilModifierElementType toMsilModifier(DotNetModifier modifier)
	{
		if(modifier == DotNetModifier.STATIC)
		{
			return MsilTokens.STATIC_KEYWORD;
		}
		if(modifier instanceof CSharpModifier)
		{
			switch((CSharpModifier) modifier)
			{
				case PUBLIC:
					return MsilTokens.PUBLIC_KEYWORD;
				case PRIVATE:
					return MsilTokens.PRIVATE_KEYWORD;
				case PROTECTED:
					return MsilTokens.PROTECTED_KEYWORD;
				case STATIC:
					return MsilTokens.STATIC_KEYWORD;
				case SEALED:
					return MsilTokens.SEALED_KEYWORD;
				case READONLY:
					break;
				case UNSAFE:
					break;
				case PARAMS: //TODO [VISTALL] handle System.ParamArrayAttribute
					break;
				case THIS:  //TODO [VISTALL] handle System.Runtime.CompilerServices.ExtensionAttribute
					break;
				case ABSTRACT:
					return MsilTokens.ABSTRACT_KEYWORD;
				case PARTIAL:
					break;
			}
		}
		return null;
	}

	private static Map<MsilEntry, PsiElement> ourCache = new ConcurrentHashMap<MsilEntry, PsiElement>();

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

			if(isInheritor((MsilClassEntry) element, DotNetTypes.System_MulticastDelegate, true))
			{
				val msilMethodEntry = (MsilMethodEntry) ContainerUtil.find(((MsilClassEntry) element).getMembers(),
						new Condition<DotNetNamedElement>()
				{
					@Override
					public boolean value(DotNetNamedElement element)
					{
						return element instanceof MsilMethodEntry && Comparing.equal(element.getName(), "Invoke");
					}
				});

				assert msilMethodEntry != null : ((MsilClassEntry) element).getPresentableQName();

				cache = new MsilMethodAsCSharpMethodDefinition((MsilClassEntry) element, msilMethodEntry);
			}
			else
			{
				cache = new MsilClassAsCSharpTypeDefinition((MsilClassEntry) element);
			}
			ourCache.put((MsilClassEntry) element, cache);
			return cache;
		}
		return element;
	}

	public static DotNetTypeRef extractToCSharp(DotNetTypeRef typeRef, PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(typeRef instanceof MsilReferenceTypeRefImpl)
		{
			PsiElement resolve = typeRef.resolve(scope);
			if(resolve == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}
			return new CSharpQualifiedTypeRef((DotNetQualifiedElement) MsilToCSharpUtil.wrap(resolve));
		}
		else if(typeRef instanceof MsilArrayTypRefImpl)
		{
			return new CSharpArrayTypeRef(extractToCSharp(((MsilArrayTypRefImpl) typeRef).getInnerType(), scope), 0);
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			val inner = extractToCSharp(((DotNetGenericWrapperTypeRef) typeRef).getInner(), scope);
			DotNetTypeRef[] arguments = ((DotNetGenericWrapperTypeRef) typeRef).getArguments();
			DotNetTypeRef[] newArguments = new DotNetTypeRef[arguments.length];
			for(int i = 0; i < newArguments.length; i++)
			{
				newArguments[i] = extractToCSharp(arguments[i], scope);
			}
			return new DotNetGenericWrapperTypeRef(inner, arguments);
		}
		return new MsilDelegateTypeRef(typeRef);
	}

	public static boolean isInheritor(DotNetTypeDeclaration typeDeclaration, String other, boolean deep)
	{
		DotNetTypeRef[] anExtends = typeDeclaration.getExtendTypeRefs();
		if(anExtends.length > 0)
		{
			for(DotNetTypeRef dotNetType : anExtends)
			{
				PsiElement psiElement = dotNetType.resolve(typeDeclaration);
				if(psiElement instanceof DotNetTypeDeclaration)
				{
					if(psiElement.isEquivalentTo(typeDeclaration))
					{
						return false;
					}

					if(Comparing.equal(((DotNetTypeDeclaration) psiElement).getPresentableQName(), other))
					{
						return true;
					}

					if(deep)
					{
						if(isInheritor((DotNetTypeDeclaration) psiElement, other, true))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
