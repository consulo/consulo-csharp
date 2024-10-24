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

package consulo.csharp.lang.impl.psi.light;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.impl.psi.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public abstract class CSharpLightLikeMethodDeclaration<S extends DotNetLikeMethodDeclaration> extends CSharpLightNamedElement<S> implements
		DotNetLikeMethodDeclaration
{
	protected S myOriginal;
	private final DotNetParameterList myParameterList;

	public CSharpLightLikeMethodDeclaration(S original, @Nullable DotNetParameterList parameterList)
	{
		super(original);
		myOriginal = original;
		myParameterList = parameterList;
	}

	@RequiredReadAction
	@Nonnull
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpLikeMethodDeclarationImplUtil.isEquivalentTo(this, another);
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return myParameterList;
	}

	@Nonnull
	@Override
	public DotNetParameter[] getParameters()
	{
		return myParameterList == null ? DotNetParameter.EMPTY_ARRAY : myParameterList.getParameters();
	}

	@Nonnull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		return myParameterList == null ? DotNetTypeRef.EMPTY_ARRAY : myParameterList.getParameterTypeRefs();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpCodeBodyProxy getCodeBlock()
	{
		return (CSharpCodeBodyProxy) myOriginal.getCodeBlock();
	}

	@Nullable
	@Override
	public DotNetType getReturnType()
	{
		return myOriginal.getReturnType();
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return myOriginal.getGenericParameterList();
	}

	@Nonnull
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

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
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
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		return myOriginal.setName(s);
	}
}
