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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightAttributeBuilder;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.msil.lang.psi.MsilModifierElementType;
import org.mustbe.consulo.msil.lang.psi.MsilModifierList;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilModifierListToCSharpModifierList extends LightElement implements DotNetModifierList
{
	private final MsilModifierList myModifierList;

	private final CSharpModifier[] myAdditional;

	public MsilModifierListToCSharpModifierList(MsilModifierList modifierList)
	{
		this(CSharpModifier.EMPTY_ARRAY, modifierList);
	}

	public MsilModifierListToCSharpModifierList(CSharpModifier[] additional, MsilModifierList modifierList)
	{
		super(PsiManager.getInstance(modifierList.getProject()), CSharpLanguage.INSTANCE);
		myAdditional = additional;
		myModifierList = modifierList;
	}

	@NotNull
	@Override
	public DotNetModifier[] getModifiers()
	{
		List<CSharpModifier> list = new ArrayList<CSharpModifier>();
		for(CSharpModifier cSharpModifier : CSharpModifier.values())
		{
			MsilModifierElementType modifierElementType = MsilToCSharpUtil.toMsilModifier(cSharpModifier);
			if(modifierElementType == null)
			{
				continue;
			}
			if(myModifierList.hasModifier(modifierElementType))
			{
				list.add(cSharpModifier);
			}
		}
		Collections.addAll(list, myAdditional);
		return list.toArray(new DotNetModifier[list.size()]);
	}

	@NotNull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		DotNetAttribute[] oldAttributes = myModifierList.getAttributes();
		List<DotNetAttribute> attributes = new ArrayList<DotNetAttribute>(oldAttributes.length);
		for(DotNetAttribute attribute : attributes)
		{
			attributes.add(attribute);
		}

		if(hasModifier(MsilTokens.SERIALIZABLE_KEYWORD))
		{
			attributes.add(new CSharpLightAttributeBuilder(myModifierList, DotNetTypes.System_Serializable));
		}
		return attributes.toArray(new DotNetAttribute[attributes.size()]);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		if(ArrayUtil.contains(modifier, myAdditional))
		{
			return true;
		}
		MsilModifierElementType modifierElementType = MsilToCSharpUtil.toMsilModifier(modifier);
		if(modifierElementType == null)
		{
			return false;
		}
		return myModifierList.hasModifier(modifierElementType);
	}

	@Override
	public boolean hasModifierInTree(@NotNull DotNetModifier modifier)
	{
		return hasModifier(modifier);
	}

	@Nullable
	@Override
	public PsiElement getModifier(IElementType elementType)
	{
		return null;
	}

	@Override
	public String toString()
	{
		return myModifierList.toString();
	}
}
