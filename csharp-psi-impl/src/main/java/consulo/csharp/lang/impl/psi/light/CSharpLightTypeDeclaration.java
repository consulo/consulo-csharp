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
import consulo.application.util.NotNullLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.type.wrapper.GenericUnwrapTool;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.03.2016
 */
public class CSharpLightTypeDeclaration extends CSharpLightNamedElement<CSharpTypeDeclaration> implements CSharpTypeDeclaration
{
	private final NotNullLazyValue<DotNetNamedElement[]> myMembersValue;
	private DotNetGenericExtractor myExtractor;

	public CSharpLightTypeDeclaration(CSharpTypeDeclaration original, DotNetGenericExtractor extractor)
	{
		super(original);
		myExtractor = extractor;
		myMembersValue = NotNullLazyValue.createValue(() ->
		{
			DotNetNamedElement[] originalMembers = myOriginal.getMembers();
			DotNetNamedElement[] members = new DotNetNamedElement[originalMembers.length];
			for(int i = 0; i < originalMembers.length; i++)
			{
				members[i] = GenericUnwrapTool.extract(originalMembers[i], myExtractor, CSharpLightTypeDeclaration.this);
			}
			return members;
		});
	}

	@Nonnull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return myOriginal.getLeftBrace();
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return myOriginal.getRightBrace();
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return myOriginal.getGenericConstraintList();
	}

	@Nonnull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return myOriginal.getGenericConstraints();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTypeDeclaration(this);
	}

	@Override
	public boolean isInterface()
	{
		return myOriginal.isInterface();
	}

	@Override
	public boolean isStruct()
	{
		return myOriginal.isStruct();
	}

	@Override
	public boolean isEnum()
	{
		return myOriginal.isEnum();
	}

	@Override
	public boolean isNested()
	{
		return myOriginal.isNested();
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return myOriginal.getExtendList();
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		DotNetTypeRef[] extendTypeRefs = myOriginal.getExtendTypeRefs();
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[extendTypeRefs.length];
		for(int i = 0; i < extendTypeRefs.length; i++)
		{
			DotNetTypeRef extendTypeRef = extendTypeRefs[i];
			typeRefs[i] = GenericUnwrapTool.exchangeTypeRef(extendTypeRef, myExtractor);
		}
		return typeRefs;
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@Nonnull String s, boolean b)
	{
		return myOriginal.isInheritor(s, b);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return myOriginal.isEquivalentTo(another);
	}

	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return myOriginal.getTypeRefForEnumConstants();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmQName()
	{
		return myOriginal.getVmQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmName()
	{
		return myOriginal.getVmName();
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

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetNamedElement[] getMembers()
	{
		return myMembersValue.getValue();
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier dotNetModifier)
	{
		return myOriginal.hasModifier(dotNetModifier);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return myOriginal.getModifierList();
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

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return myOriginal.getNameIdentifier();
	}

	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return myOriginal.setName(name);
	}
}
