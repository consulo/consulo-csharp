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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public abstract class CSharpLightLikeMethodDeclaration<S extends DotNetLikeMethodDeclaration> extends CSharpLightNamedElement<S> implements
		DotNetLikeMethodDeclaration
{
	protected S myOriginal;
	private final DotNetTypeRef myReturnTypeRef;
	private final DotNetParameterList myParameterTypes;

	public CSharpLightLikeMethodDeclaration(S original, DotNetTypeRef returnTypeRef, @NotNull DotNetParameterList parameterList)
	{
		super(original);
		myOriginal = original;
		myReturnTypeRef = returnTypeRef;
		myParameterTypes = parameterList;
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return myParameterTypes;
	}

	@NotNull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameterTypes.getParameters();
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		return myParameterTypes.getParameterTypesForRuntime();
	}

	@Nullable
	@Override
	public PsiElement getCodeBlock()
	{
		return myOriginal.getCodeBlock();
	}

	@Nullable
	@Override
	public DotNetType getReturnType()
	{
		return myOriginal.getReturnType();
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRef;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return myOriginal.getGenericParameterList();
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myOriginal.getGenericParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myOriginal.getGenericParametersCount();
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myOriginal.hasModifier(modifier);
	}

	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myOriginal.getModifierList();
	}

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myOriginal.getPresentableParentQName();
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myOriginal.getPresentableQName();
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return myOriginal.setName(s);
	}
}
