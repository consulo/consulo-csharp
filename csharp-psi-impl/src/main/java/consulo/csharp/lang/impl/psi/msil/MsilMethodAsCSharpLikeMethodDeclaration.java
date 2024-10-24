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
import consulo.application.util.NotNullLazyValue;
import consulo.application.util.NullableLazyValue;
import consulo.csharp.lang.impl.psi.light.CSharpLightGenericConstraintList;
import consulo.csharp.lang.impl.psi.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.msil.impl.lang.psi.MsilTokens;
import consulo.msil.lang.psi.MsilMethodEntry;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public abstract class MsilMethodAsCSharpLikeMethodDeclaration extends MsilElementWrapper<MsilMethodEntry> implements DotNetLikeMethodDeclaration
{
	private MsilModifierListToCSharpModifierList myModifierList;
	private DotNetGenericParameterList myGenericParameterList;
	protected NullableLazyValue<CSharpLightGenericConstraintList> myGenericConstraintListValue;

	private final NotNullLazyValue<DotNetTypeRef> myReturnTypeRefValue;
	private final NotNullLazyValue<DotNetTypeRef[]> myParameterTypeRefsValue;

	@RequiredReadAction
	public MsilMethodAsCSharpLikeMethodDeclaration(PsiElement parent, MsilMethodEntry methodEntry)
	{
		this(parent, CSharpModifier.EMPTY_ARRAY, methodEntry);
	}

	@RequiredReadAction
	public MsilMethodAsCSharpLikeMethodDeclaration(PsiElement parent, @Nonnull CSharpModifier[] modifiers, MsilMethodEntry methodEntry)
	{
		super(parent, methodEntry);
		myModifierList = new MsilModifierListToCSharpModifierList(modifiers, this, methodEntry.getModifierList());

		myReturnTypeRefValue = NotNullLazyValue.createValue(() -> MsilToCSharpUtil.extractToCSharp(myOriginal.getReturnTypeRef()));
		myParameterTypeRefsValue = NotNullLazyValue.createValue(() ->
		{
			DotNetTypeRef[] parameters = myOriginal.getParameterTypeRefs();
			DotNetTypeRef[] refs = new DotNetTypeRef[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				refs[i] = MsilToCSharpUtil.extractToCSharp(parameters[i]);
			}
			return refs;
		});
	}

	protected void setGenericParameterList(@Nonnull DotNetGenericParameterListOwner owner, @Nonnull GenericParameterContext genericParameterContext)
	{
		DotNetGenericParameterList genericParameterList = owner.getGenericParameterList();
		myGenericParameterList = MsilGenericParameterListAsCSharpGenericParameterList.build(this, genericParameterList, genericParameterContext);
		myGenericConstraintListValue = new NullableLazyValue<CSharpLightGenericConstraintList>()
		{
			@Nullable
			@Override
			@RequiredReadAction
			protected CSharpLightGenericConstraintList compute()
			{
				return MsilAsCSharpBuildUtil.buildConstraintList(myGenericParameterList);
			}
		};
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpLikeMethodDeclarationImplUtil.isEquivalentTo(this, another);
	}

	@RequiredReadAction
	@Nonnull
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpLikeMethodDeclarationImplUtil.getParametersInfos(this);
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myOriginal.getContainingFile();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetType getReturnType()
	{
		throw new IllegalArgumentException();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRefValue.getValue();
	}

	@Nonnull
	@Override
	public CSharpCodeBodyProxy getCodeBlock()
	{
		return CSharpCodeBodyProxy.EMPTY;
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return myGenericParameterList;
	}

	@Nonnull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myGenericParameterList == null ? DotNetGenericParameter.EMPTY_ARRAY : myGenericParameterList.getParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myGenericParameterList == null ? 0 : myGenericParameterList.getGenericParametersCount();
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return myModifierList.hasModifier(modifier);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myModifierList;
	}

	@Nonnull
	@Override
	public DotNetTypeRef[] getParameterTypeRefs()
	{
		return myParameterTypeRefsValue.getValue();
	}

	@Nullable
	@Override
	public DotNetParameterList getParameterList()
	{
		return myOriginal.getParameterList();
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetParameter[] getParameters()
	{
		DotNetParameter[] parameters = myOriginal.getParameters();
		boolean hasVararg = myOriginal.hasModifier(MsilTokens.VARARG_KEYWORD);
		DotNetParameter[] newParameters = new DotNetParameter[parameters.length + (hasVararg ? 1 : 0)];
		for(int i = 0; i < parameters.length; i++)
		{
			DotNetParameter parameter = parameters[i];
			newParameters[i] = new MsilParameterAsCSharpParameter(this, parameter, this, i);
		}
		if(hasVararg)
		{
			newParameters[parameters.length] = new MsilVarargParameterAsCSharpParameter(this);
		}
		return newParameters;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myOriginal.getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myOriginal.getPresentableQName();
	}

	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@Override
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getPresentableQName();
	}
}
