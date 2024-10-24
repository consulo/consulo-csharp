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
import consulo.application.util.NullableLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.light.CSharpLightAttributeBuilder;
import consulo.csharp.lang.impl.psi.light.CSharpLightAttributeWithSelfTypeBuilder;
import consulo.csharp.lang.impl.psi.source.CSharpModifierListImplUtil;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.externalAttributes.ExternalAttributeArgumentNode;
import consulo.dotnet.externalAttributes.ExternalAttributeHolder;
import consulo.dotnet.externalAttributes.ExternalAttributeNode;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.impl.externalAttributes.ExternalAttributesUtil;
import consulo.language.psi.PsiElement;
import consulo.msil.impl.lang.psi.MsilTokens;
import consulo.util.collection.ArrayUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilModifierListToCSharpModifierList extends MsilElementWrapper<DotNetModifierList> implements CSharpModifierList
{
	private static final String[] ourAttributeBans = new String[]{
			DotNetTypes.System.ParamArrayAttribute,
			DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute,
			DotNetTypes2.System.Runtime.CompilerServices.AsyncStateMachineAttribute
	};

	private final DotNetModifierList myModifierList;

	private final CSharpModifier[] myAdditional;
	private List<DotNetAttribute> myAdditionalAttributes = Collections.emptyList();

	private NullableLazyValue<ExternalAttributeHolder> myAttributeHolderValue;

	private final Map<CSharpModifier, Boolean> myModifiersState = new ConcurrentHashMap<>();

	@RequiredReadAction
	public MsilModifierListToCSharpModifierList(@Nonnull PsiElement parent, @Nonnull DotNetModifierList modifierList)
	{
		this(CSharpModifier.EMPTY_ARRAY, parent, modifierList);
	}

	@RequiredReadAction
	public MsilModifierListToCSharpModifierList(@Nonnull CSharpModifier[] additional, @Nonnull PsiElement parent, @Nonnull DotNetModifierList modifierList)
	{
		super(parent, modifierList);
		myAdditional = additional;
		myModifierList = modifierList;

		if(myModifierList.hasModifier(MsilTokens.SERIALIZABLE_KEYWORD))
		{
			addAdditionalAttribute(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes.System.Serializable));
		}

		if(myModifierList.hasModifier(MsilTokens.BRACKET_OUT_KEYWORD))
		{
			addAdditionalAttribute(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes2.System.Runtime.InteropServices.OutAttribute));
		}

		if(myModifierList.hasModifier(MsilTokens.BRACKET_IN_KEYWORD))
		{
			addAdditionalAttribute(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes2.System.Runtime.InteropServices.InAttribute));
		}

		myAttributeHolderValue = NullableLazyValue.of(() -> ExternalAttributesUtil.findHolder(myModifierList));
	}

	public void addAdditionalAttribute(@Nonnull DotNetAttribute attribute)
	{
		if(myAdditionalAttributes.isEmpty())
		{
			myAdditionalAttributes = new ArrayList<>(5);
		}
		myAdditionalAttributes.add(attribute);
	}

	@Override
	public void addModifier(@Nonnull DotNetModifier modifier)
	{

	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitModifierList(this);
	}

	@Override
	public void removeModifier(@Nonnull DotNetModifier modifier)
	{

	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetModifier[] getModifiers()
	{
		Set<CSharpModifier> all = new HashSet<>();
		for(CSharpModifier cSharpModifier : CSharpModifier.values())
		{
			if(MsilToCSharpUtil.hasCSharpInMsilModifierList(cSharpModifier, myModifierList))
			{
				all.add(cSharpModifier);
			}
		}
		Collections.addAll(all, myAdditional);
		return all.toArray(CSharpModifier[]::new);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		DotNetAttribute[] oldAttributes = myModifierList.getAttributes();
		List<DotNetAttribute> attributes = new ArrayList<>(oldAttributes.length + myAdditionalAttributes.size());
		for(DotNetAttribute oldAttribute : oldAttributes)
		{
			DotNetTypeDeclaration resolvedType = oldAttribute.resolveToType();
			if(resolvedType != null && ArrayUtil.contains(resolvedType.getVmQName(), ourAttributeBans))
			{
				continue;
			}
			attributes.add(oldAttribute);
		}
		attributes.addAll(myAdditionalAttributes);

		ExternalAttributeHolder holder = myAttributeHolderValue.getValue();

		if(holder != null)
		{
			List<ExternalAttributeNode> nodes = findAttributes(holder);
			for(ExternalAttributeNode node : nodes)
			{
				CSharpLightAttributeWithSelfTypeBuilder builder = new CSharpLightAttributeWithSelfTypeBuilder(myModifierList, node.getName());

				for(ExternalAttributeArgumentNode argumentNode : node.getArguments())
				{
					builder.addParameterExpression(argumentNode.toJavaObject());
				}
				attributes.add(builder);
			}
		}
		return attributes.toArray(new DotNetAttribute[attributes.size()]);
	}

	@Nonnull
	public List<ExternalAttributeNode> findAttributes(ExternalAttributeHolder holder)
	{
		return Collections.emptyList();
	}

	@Override
	@RequiredReadAction
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return myModifiersState.computeIfAbsent(CSharpModifier.as(modifier), it -> CSharpModifierListImplUtil.hasModifier(this, it));
	}

	@Override
	public boolean hasModifierInTree(@Nonnull DotNetModifier modifier)
	{
		CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
		if(ArrayUtil.contains(cSharpModifier, myAdditional))
		{
			return true;
		}
		return MsilToCSharpUtil.hasCSharpInMsilModifierList(cSharpModifier, myModifierList);
	}

	@Nullable
	@Override
	public PsiElement getModifierElement(DotNetModifier modifier)
	{
		return null;
	}

	@Nonnull
	@Override
	public List<PsiElement> getModifierElements(@Nonnull DotNetModifier modifier)
	{
		return Collections.emptyList();
	}

	@Override
	public String toString()
	{
		return myModifierList.toString();
	}

	@Nonnull
	@Override
	public CSharpAttributeList[] getAttributeLists()
	{
		return CSharpAttributeList.EMPTY_ARRAY;
	}
}
