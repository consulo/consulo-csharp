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

import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilModifierList;
import org.mustbe.consulo.msil.lang.psi.MsilPropertyEntry;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 28.05.14
 */
public class MsilPropertyAsCSharpArrayMethodDefinition extends LightElement implements CSharpArrayMethodDeclaration
{
	private final MsilPropertyEntry myPropertyEntry;
	private final MsilModifierListToCSharpModifierList myModifierList;

	private final DotNetParameter[] myParameters;

	public MsilPropertyAsCSharpArrayMethodDefinition(MsilPropertyEntry propertyEntry, List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		super(PsiManager.getInstance(propertyEntry.getProject()), CSharpLanguage.INSTANCE);
		myPropertyEntry = propertyEntry;
		myModifierList = new MsilModifierListToCSharpModifierList(MsilPropertyAsCSharpPropertyDefinition.getAdditionalModifiers(pairs),
				(MsilModifierList) propertyEntry.getModifierList());

		setNavigationElement(propertyEntry); //TODO [VISTALL] generator from MSIL to C#

		Pair<DotNetXXXAccessor, MsilMethodEntry> p = pairs.get(0);

		DotNetParameter firstParameter = p.getSecond().getParameters()[0];
		myParameters = new DotNetParameter[] {new MsilParameterAsCSharpParameter(firstParameter, this, 0)};
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myPropertyEntry.getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myPropertyEntry.getPresentableQName();
	}

	@Override
	public String getName()
	{
		return "Item";
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public String toString()
	{
		return getPresentableQName();
	}

	@NotNull
	@Override
	public DotNetXXXAccessor[] getAccessors()
	{
		return new DotNetXXXAccessor[0];
	}

	@NotNull
	@Override
	public DotNetType getReturnType()
	{
		throw new IllegalArgumentException();
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return MsilToCSharpUtil.extractToCSharp(myPropertyEntry.toTypeRef(false), myPropertyEntry);
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myModifierList.hasModifier(modifier);
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myModifierList;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		DotNetParameter[] parameters = getParameters();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			typeRefs[i] = parameter.toTypeRef(false);
		}
		return typeRefs;
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameters;
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return null;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return DotNetGenericParameter.EMPTY_ARRAY;
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}
}
