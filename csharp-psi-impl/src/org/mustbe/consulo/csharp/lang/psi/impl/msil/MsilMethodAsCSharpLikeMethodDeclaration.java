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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilModifierList;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpLikeMethodDeclaration extends MsilElementWrapper<MsilMethodEntry> implements DotNetLikeMethodDeclaration
{
	private MsilModifierListToCSharpModifierList myModifierList;

	public MsilMethodAsCSharpLikeMethodDeclaration(PsiElement parent, MsilMethodEntry methodEntry)
	{
		this(parent, CSharpModifier.EMPTY_ARRAY, methodEntry);
	}

	public MsilMethodAsCSharpLikeMethodDeclaration(PsiElement parent, CSharpModifier[] modifiers, MsilMethodEntry methodEntry)
	{
		super(parent, methodEntry);
		myModifierList = new MsilModifierListToCSharpModifierList(modifiers, (MsilModifierList) methodEntry.getModifierList());
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myMsilElement.getContainingFile();
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
		return MsilToCSharpUtil.extractToCSharp(myMsilElement.getReturnTypeRef(), myMsilElement);
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
		return myMsilElement.getGenericParameterList();
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myMsilElement.getGenericParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myMsilElement.getGenericParametersCount();
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
		DotNetTypeRef[] parameters = myMsilElement.getParameterTypeRefs();
		DotNetTypeRef[] refs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			refs[i] = MsilToCSharpUtil.extractToCSharp(parameters[i], myMsilElement);
		}
		return refs;
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return myMsilElement.getParameterList();
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		DotNetParameter[] parameters = myMsilElement.getParameters();
		DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			newParameters[i] = new MsilParameterAsCSharpParameter(this, parameter, this, i);
		}
		return newParameters;
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myMsilElement.getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myMsilElement.getPresentableQName();
	}

	@Override
	public String getName()
	{
		return myMsilElement.getName();
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
}
