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

package consulo.csharp.lang.psi.impl.msil;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.csharp.lang.psi.impl.light.builder.CSharpLightNamespaceDeclarationBuilder;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetInheritUtil;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericWrapperTypeRef;
import consulo.dotnet.resolve.DotNetPointerTypeRef;
import consulo.dotnet.resolve.DotNetRefTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.msil.lang.psi.MsilModifierElementType;
import consulo.msil.lang.psi.MsilTokens;
import consulo.msil.lang.psi.impl.type.MsilArrayTypRefImpl;
import consulo.msil.lang.psi.impl.type.MsilNativeTypeRefImpl;
import consulo.msil.lang.psi.impl.type.MsilPointerTypeRefImpl;
import consulo.msil.lang.psi.impl.type.MsilRefTypeRefImpl;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class MsilToCSharpUtil
{
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

	@NotNull
	@RequiredReadAction
	public static PsiElement wrap(@NotNull PsiElement element, @Nullable PsiElement parent)
	{
		return wrap(element, parent, new GenericParameterContext(null));
	}

	@NotNull
	@RequiredReadAction
	public static PsiElement wrap(@NotNull PsiElement element, @Nullable PsiElement parent, @NotNull GenericParameterContext context)
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
				return CachedValuesManager.getCachedValue(element, () ->
				{
					PsiElement thisParent = null;
					String parentQName = ((MsilClassEntry) element).getPresentableParentQName();
					if(!StringUtil.isEmpty(parentQName))
					{
						thisParent = new CSharpLightNamespaceDeclarationBuilder(element.getProject(), parentQName);
					}

					PsiElement wrapElement = wrapToDelegateMethod((MsilClassEntry) element, thisParent, context);
					if(wrapElement == null)
					{
						wrapElement = new MsilClassAsCSharpTypeDefinition(thisParent, (MsilClassEntry) element, context);
					}

					return CachedValueProvider.Result.create(wrapElement, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
				});
			}
		}
		return element;
	}

	@Nullable
	@RequiredReadAction
	private static CSharpMethodDeclaration wrapToDelegateMethod(@NotNull MsilClassEntry typeDeclaration, @Nullable PsiElement parent, @NotNull GenericParameterContext context)
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

	@NotNull
	@RequiredReadAction
	public static DotNetTypeRef extractToCSharp(@NotNull DotNetTypeRef typeRef, @NotNull PsiElement scope)
	{
		if(typeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(typeRef instanceof MsilNativeTypeRefImpl)
		{
			return new CSharpTypeRefByQName(scope, typeRef.toString());
		}
		else if(typeRef instanceof MsilArrayTypRefImpl)
		{
			int[] lowerValues = ((MsilArrayTypRefImpl) typeRef).getLowerValues();
			return new CSharpArrayTypeRef(scope, extractToCSharp(((MsilArrayTypRefImpl) typeRef).getInnerTypeRef(), scope), lowerValues.length == 0 ? 0 : lowerValues.length - 1);
		}
		else if(typeRef instanceof MsilPointerTypeRefImpl)
		{
			return new CSharpPointerTypeRef(scope, extractToCSharp(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), scope));
		}
		else if(typeRef instanceof MsilRefTypeRefImpl)
		{
			DotNetTypeRef innerTypeRef = extractToCSharp(((DotNetRefTypeRef) typeRef).getInnerTypeRef(), scope);
			return new CSharpRefTypeRef(scope.getProject(), CSharpRefTypeRef.Type.ref, innerTypeRef);
		}
		else if(typeRef instanceof DotNetGenericWrapperTypeRef)
		{
			DotNetTypeRef innerTypeRef = ((DotNetGenericWrapperTypeRef) typeRef).getInnerTypeRef();
			DotNetTypeRef[] arguments = ((DotNetGenericWrapperTypeRef) typeRef).getArgumentTypeRefs();

			DotNetTypeRef inner = extractToCSharp(innerTypeRef, scope);

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
					list.add(extractToCSharp(anotherArray[i], scope));
				}

				list = ContainerUtil.reverse(list);

				return new CSharpGenericWrapperTypeRef(scope.getProject(), inner, ContainerUtil.toArray(list, DotNetTypeRef.ARRAY_FACTORY));
			}
			else  // fallback
			{
				DotNetTypeRef[] newArguments = new DotNetTypeRef[arguments.length];
				for(int i = 0; i < newArguments.length; i++)
				{
					newArguments[i] = extractToCSharp(arguments[i], scope);
				}

				return new CSharpGenericWrapperTypeRef(scope.getProject(), inner, newArguments);
			}
		}
		return new MsilDelegateTypeRef(scope, typeRef);
	}
}
