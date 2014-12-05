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

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpLightTypeDeclaration extends CSharpLightNamedElement<CSharpTypeDeclaration> implements CSharpTypeDeclaration
{
	private final DotNetGenericExtractor myExtractor;

	public CSharpLightTypeDeclaration(@NotNull CSharpTypeDeclaration original, @NotNull DotNetGenericExtractor extractor)
	{
		super(original);
		myExtractor = extractor;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
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

	@Override
	@Nullable
	public DotNetTypeList getExtendList()
	{
		return myOriginal.getExtendList();
	}

	@Override
	@NotNull
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return myOriginal.getExtendTypeRefs();
	}

	@Override
	public boolean isInheritor(@NotNull DotNetTypeDeclaration typeDeclaration, boolean b)
	{
		return myOriginal.isInheritor(typeDeclaration, b);
	}

	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return myOriginal.getTypeRefForEnumConstants();
	}

	@Override
	@Nullable
	public String getVmQName()
	{
		return myOriginal.getVmQName();
	}

	@Override
	@Nullable
	public String getVmName()
	{
		return myOriginal.getVmName();
	}

	@Override
	@Nullable
	public String getPresentableParentQName()
	{
		return myOriginal.getPresentableParentQName();
	}

	@Override
	@Nullable
	public String getPresentableQName()
	{
		return myOriginal.getPresentableQName();
	}

	@Override
	public PsiElement getLeftBrace()
	{
		return myOriginal.getLeftBrace();
	}

	@Override
	public PsiElement getRightBrace()
	{
		return myOriginal.getRightBrace();
	}

	@Override
	@NotNull
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return myOriginal.getGenericConstraints();
	}

	@Override
	@Nullable
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return myOriginal.getGenericConstraintList();
	}

	@Override
	@NotNull
	@LazyInstance
	public DotNetNamedElement[] getMembers()
	{
		DotNetNamedElement[] members = myOriginal.getMembers();
		DotNetNamedElement[] newMembers = new DotNetNamedElement[members.length];
		for(int i = 0; i < members.length; i++)
		{
			DotNetNamedElement member = members[i];
			newMembers[i] = GenericUnwrapTool.extract(member, myExtractor);
		}
		return newMembers;
	}

	@Override
	@Nullable
	public PsiElement getNameIdentifier()
	{
		return myOriginal.getNameIdentifier();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myOriginal.getGenericParametersCount();
	}

	@Override
	@NotNull
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myOriginal.getGenericParameters();
	}

	@Override
	@Nullable
	public DotNetGenericParameterList getGenericParameterList()
	{
		return myOriginal.getGenericParameterList();
	}

	@Override
	@Nullable
	public DotNetModifierList getModifierList()
	{
		return myOriginal.getModifierList();
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return myOriginal.hasModifier(modifier);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return myOriginal.setName(name);
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpTypeDeclarationImplUtil.isEquivalentTo(this, another);
	}
}
