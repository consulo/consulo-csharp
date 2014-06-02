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
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
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
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpLikeMethodDeclaration extends LightElement implements DotNetLikeMethodDeclaration
{
	protected final MsilMethodEntry myMethodEntry;
	private MsilModifierListToCSharpModifierList myModifierList;

	public MsilMethodAsCSharpLikeMethodDeclaration(MsilMethodEntry methodEntry)
	{
		this(CSharpModifier.EMPTY_ARRAY, methodEntry);
	}

	public MsilMethodAsCSharpLikeMethodDeclaration(CSharpModifier[] modifiers, MsilMethodEntry methodEntry)
	{
		super(PsiManager.getInstance(methodEntry.getProject()), CSharpLanguage.INSTANCE);
		myMethodEntry = methodEntry;
		myModifierList = new MsilModifierListToCSharpModifierList(modifiers, (MsilModifierList) methodEntry.getModifierList());

		setNavigationElement(methodEntry); //TODO [VISTALL] generator from MSIL to C#
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myMethodEntry.getContainingFile();
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
		return MsilToCSharpUtil.extractToCSharp(myMethodEntry.getReturnTypeRef(), myMethodEntry);
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
		return myMethodEntry.getGenericParameterList();
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myMethodEntry.getGenericParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myMethodEntry.getGenericParametersCount();
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
	public DotNetTypeRef[] getParameterTypesForRuntime()
	{
		DotNetTypeRef[] parameters = myMethodEntry.getParameterTypesForRuntime();
		DotNetTypeRef[] refs = new DotNetTypeRef[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			refs[i] = MsilToCSharpUtil.extractToCSharp(parameters[i], myMethodEntry);
		}
		return refs;
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return myMethodEntry.getParameterList();
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		DotNetParameter[] parameters = myMethodEntry.getParameters();
		DotNetParameter[] newParameters = new DotNetParameter[parameters.length];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			newParameters[i] = new MsilParameterAsCSharpParameter(parameter, this, i);
		}
		return newParameters;
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myMethodEntry.getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myMethodEntry.getPresentableQName();
	}

	@Override
	public String getName()
	{
		return myMethodEntry.getName();
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
