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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.impl.light.CSharpLightGenericConstraintList;
import consulo.csharp.lang.psi.impl.source.CSharpLikeMethodDeclarationImplUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.msil.lang.psi.MsilTokens;

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
	public MsilMethodAsCSharpLikeMethodDeclaration(PsiElement parent, @NotNull CSharpModifier[] modifiers, MsilMethodEntry methodEntry)
	{
		super(parent, methodEntry);
		myModifierList = new MsilModifierListToCSharpModifierList(modifiers, this, methodEntry.getModifierList());

		myReturnTypeRefValue = NotNullLazyValue.createValue(() -> MsilToCSharpUtil.extractToCSharp(myOriginal.getReturnTypeRef(), myOriginal));
		myParameterTypeRefsValue = NotNullLazyValue.createValue(() ->
		{
			DotNetTypeRef[] parameters = myOriginal.getParameterTypeRefs();
			DotNetTypeRef[] refs = new DotNetTypeRef[parameters.length];
			for(int i = 0; i < parameters.length; i++)
			{
				refs[i] = MsilToCSharpUtil.extractToCSharp(parameters[i], myOriginal);
			}
			return refs;
		});
	}

	protected void setGenericParameterList(@NotNull DotNetGenericParameterListOwner owner, @NotNull GenericParameterContext genericParameterContext)
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
	@NotNull
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
	@NotNull
	@Override
	public DotNetType getReturnType()
	{
		throw new IllegalArgumentException();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRefValue.getValue();
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
		return myGenericParameterList;
	}

	@NotNull
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
	public boolean hasModifier(@NotNull DotNetModifier modifier)
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

	@NotNull
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

	@NotNull
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
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getPresentableQName();
	}
}
