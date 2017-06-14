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
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.csharp.lang.psi.impl.light.CSharpLightAttributeBuilder;
import consulo.csharp.lang.psi.impl.light.CSharpLightAttributeWithSelfTypeBuilder;
import consulo.csharp.lang.psi.impl.source.CSharpModifierListImplUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.externalAttributes.ExternalAttributeArgumentNode;
import consulo.dotnet.externalAttributes.ExternalAttributeHolder;
import consulo.dotnet.externalAttributes.ExternalAttributeNode;
import consulo.dotnet.externalAttributes.ExternalAttributesUtil;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.msil.lang.psi.MsilTokens;

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

	@RequiredReadAction
	public MsilModifierListToCSharpModifierList(@NotNull PsiElement parent, @NotNull DotNetModifierList modifierList)
	{
		this(CSharpModifier.EMPTY_ARRAY, parent, modifierList);
	}

	@RequiredReadAction
	public MsilModifierListToCSharpModifierList(@NotNull CSharpModifier[] additional, @NotNull PsiElement parent, @NotNull DotNetModifierList modifierList)
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

	public void addAdditionalAttribute(@NotNull DotNetAttribute attribute)
	{
		if(myAdditionalAttributes.isEmpty())
		{
			myAdditionalAttributes = new ArrayList<>(5);
		}
		myAdditionalAttributes.add(attribute);
	}

	@Override
	public void addModifier(@NotNull DotNetModifier modifier)
	{

	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitModifierList(this);
	}

	@Override
	public void removeModifier(@NotNull DotNetModifier modifier)
	{

	}

	@NotNull
	@Override
	public DotNetModifier[] getModifiers()
	{
		List<CSharpModifier> list = new ArrayList<>();
		for(CSharpModifier cSharpModifier : CSharpModifier.values())
		{
			if(MsilToCSharpUtil.hasCSharpInMsilModifierList(cSharpModifier, myModifierList))
			{
				list.add(cSharpModifier);
			}
		}
		Collections.addAll(list, myAdditional);
		return list.toArray(new DotNetModifier[list.size()]);
	}

	@RequiredReadAction
	@NotNull
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

	@NotNull
	public List<ExternalAttributeNode> findAttributes(ExternalAttributeHolder holder)
	{
		return Collections.emptyList();
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return CSharpModifierListImplUtil.hasModifier(this, modifier);
	}

	@Override
	public boolean hasModifierInTree(@NotNull DotNetModifier modifier)
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

	@NotNull
	@Override
	public List<PsiElement> getModifierElements(@NotNull DotNetModifier modifier)
	{
		return Collections.emptyList();
	}

	@Override
	public String toString()
	{
		return myModifierList.toString();
	}

	@NotNull
	@Override
	public CSharpAttributeList[] getAttributeLists()
	{
		return CSharpAttributeList.EMPTY_ARRAY;
	}
}
