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

package consulo.csharp.lang.impl.psi.msil;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.application.util.ParameterizedCachedValue;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightNamespaceDeclarationBuilder;
import consulo.csharp.lang.impl.psi.source.resolve.type.*;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.psi.resolve.DotNetPointerTypeRef;
import consulo.dotnet.psi.resolve.DotNetRefTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.msil.impl.lang.psi.MsilModifierElementType;
import consulo.msil.impl.lang.psi.MsilTokens;
import consulo.msil.impl.lang.psi.impl.type.MsilArrayTypRefImpl;
import consulo.msil.impl.lang.psi.impl.type.MsilNativeTypeRefImpl;
import consulo.msil.impl.lang.psi.impl.type.MsilPointerTypeRefImpl;
import consulo.msil.impl.lang.psi.impl.type.MsilRefTypeRefImpl;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import consulo.util.lang.Comparing;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class MsilToCSharpUtil
{
	private static final Key<ParameterizedCachedValue<PsiElement, GenericParameterContext>> WRAP_TYPE_KEY = Key.create("WRAP_TYPE_CACHE");

	@RequiredReadAction
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
				if(hasModifierInParentIfType(modifierList, MsilTokens.INTERFACE_KEYWORD))
				{
					return false;
				}
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
	@RequiredReadAction
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

	@RequiredReadAction
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

	@Nonnull
	@RequiredReadAction
	public static PsiElement wrap(@Nonnull PsiElement element, @Nullable PsiElement parent)
	{
		return wrap(element, parent, new GenericParameterContext(null));
	}

	@Nonnull
	@RequiredReadAction
	public static PsiElement wrap(@Nonnull PsiElement element, @Nullable PsiElement parent, @Nonnull GenericParameterContext context)
	{
		if(element instanceof MsilClassEntry)
		{
			if(((MsilClassEntry) element).isNested() && parent == null)
			{
				throw new IllegalArgumentException("can't wrap without parent: " + ((MsilClassEntry) element).getVmQName());
			}

			if(parent != null)
			{
				PsiElement wrapElement = wrapToDelegateMethod((MsilClassEntry) element, parent, context);
				if(wrapElement == null)
				{
					wrapElement = new MsilClassAsCSharpTypeDefinition(parent, (MsilClassEntry) element, context);
				}
				return wrapElement;
			}
			else
			{
				return CachedValuesManager.getManager(element.getProject()).getParameterizedCachedValue(element, WRAP_TYPE_KEY, paramContext -> {
					PsiElement thisParent = null;
					String parentQName = ((MsilClassEntry) element).getPresentableParentQName();
					if(!StringUtil.isEmpty(parentQName))
					{
						thisParent = new CSharpLightNamespaceDeclarationBuilder(element.getProject(), parentQName);
					}

					PsiElement wrapElement = wrapToDelegateMethod((MsilClassEntry) element, thisParent, paramContext);
					if(wrapElement == null)
					{
						wrapElement = new MsilClassAsCSharpTypeDefinition(thisParent, (MsilClassEntry) element, paramContext);
					}

					return CachedValueProvider.Result.create(wrapElement, PsiModificationTracker.MODIFICATION_COUNT);
				}, false, context);
			}
		}
		return element;
	}

	@Nullable
	@RequiredReadAction
	private static CSharpMethodDeclaration wrapToDelegateMethod(@Nonnull MsilClassEntry typeDeclaration, @Nullable PsiElement parent, @Nonnull GenericParameterContext context)
	{
		if(DotNetInheritUtil.isInheritor(typeDeclaration, DotNetTypes.System.MulticastDelegate, false))
		{
			MsilMethodEntry msilMethodEntry = (MsilMethodEntry) ContainerUtil.find((typeDeclaration).getMembers(), element -> element instanceof MsilMethodEntry && Comparing.equal(element.getName(),
					"Invoke"));

			assert msilMethodEntry != null : typeDeclaration.getPresentableQName();

			return new MsilMethodAsCSharpMethodDeclaration(parent, typeDeclaration, context, msilMethodEntry);
		}
		else
		{
			return null;
		}
	}

	@Nonnull
	@RequiredReadAction
	public static DotNetTypeRef extractToCSharp(@Nonnull DotNetTypeRef typeRef)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		Project project = typeRef.getProject();
		GlobalSearchScope resolveScope = typeRef.getResolveScope();

		if(typeRef instanceof MsilNativeTypeRefImpl)
		{
			return new CSharpTypeRefByQName(project, resolveScope, typeRef.toString());
		}
		else if(typeRef instanceof MsilArrayTypRefImpl)
		{
			int[] lowerValues = ((MsilArrayTypRefImpl) typeRef).getLowerValues();
			return new CSharpArrayTypeRef(project, resolveScope, extractToCSharp(((MsilArrayTypRefImpl) typeRef).getInnerTypeRef()), lowerValues.length == 0 ? 0 : lowerValues.length - 1);
		}
		else if(typeRef instanceof MsilPointerTypeRefImpl)
		{
			return new CSharpPointerTypeRef(extractToCSharp(((DotNetPointerTypeRef) typeRef).getInnerTypeRef()));
		}
		else if(typeRef instanceof MsilRefTypeRefImpl)
		{
			DotNetTypeRef innerTypeRef = extractToCSharp(((DotNetRefTypeRef) typeRef).getInnerTypeRef());
			return new CSharpRefTypeRef(project, resolveScope, CSharpRefTypeRef.Type.ref, innerTypeRef);
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetTypeRef innerTypeRef = ((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef();
			DotNetTypeRef[] arguments = ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs();

			DotNetTypeRef inner = extractToCSharp(innerTypeRef);

			PsiElement element = inner.resolve().getElement();
			if(element instanceof DotNetGenericParameterListOwner)
			{
				int genericParametersCount = ((DotNetGenericParameterListOwner) element).getGenericParametersCount();
				if(genericParametersCount == 0)
				{
					return inner;
				}

				DotNetTypeRef[] anotherArray = ArrayUtil.reverseArray(arguments);
				List<DotNetTypeRef> list = new ArrayList<>(genericParametersCount);
				for(int i = 0; i < genericParametersCount; i++)
				{
					list.add(extractToCSharp(anotherArray[i]));
				}

				list = ContainerUtil.reverse(list);

				return new CSharpGenericWrapperTypeRef(project, resolveScope, inner, ContainerUtil.toArray(list, DotNetTypeRef.ARRAY_FACTORY));
			}
			else  // fallback
			{
				DotNetTypeRef[] newArguments = new DotNetTypeRef[arguments.length];
				for(int i = 0; i < newArguments.length; i++)
				{
					newArguments[i] = extractToCSharp(arguments[i]);
				}

				return new CSharpGenericWrapperTypeRef(project, resolveScope, inner, newArguments);
			}
		}
		return new MsilDelegateTypeRef(typeRef);
	}
}
