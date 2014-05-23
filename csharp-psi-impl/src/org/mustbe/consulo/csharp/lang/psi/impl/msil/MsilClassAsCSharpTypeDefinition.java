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
import java.util.List;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDeclarationImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.MsilToCSharpTypeRef;
import org.mustbe.consulo.dotnet.lang.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.MsilHelper;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilFieldEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilModifierList;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 22.05.14
 */
public class MsilClassAsCSharpTypeDefinition extends LightElement implements CSharpTypeDeclaration
{
	private final MsilClassEntry myClassEntry;
	private MsilModifierListToCSharpModifierList myModifierList;

	public MsilClassAsCSharpTypeDefinition(MsilClassEntry classEntry)
	{
		super(PsiManager.getInstance(classEntry.getProject()), CSharpLanguage.INSTANCE);
		myModifierList = new MsilModifierListToCSharpModifierList((MsilModifierList) classEntry.getModifierList());
		myClassEntry = classEntry;
		setNavigationElement(classEntry); //TODO [VISTALL] generator from MSIL to C#
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myClassEntry.getContainingFile();
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		if(another instanceof DotNetTypeDeclaration)
		{
			return Comparing.equal(getPresentableQName(), ((DotNetTypeDeclaration) another).getPresentableQName());
		}
		return super.isEquivalentTo(another);
	}

	@Override
	public boolean hasExtensions()
	{
		return false;
	}

	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return new CSharpGenericConstraint[0];
	}

	@Override
	public boolean isInterface()
	{
		return myClassEntry.isInterface();
	}

	@Override
	public boolean isStruct()
	{
		return myClassEntry.isStruct();
	}

	@Override
	public boolean isEnum()
	{
		return myClassEntry.isEnum();
	}

	@Override
	public boolean isInheritAllowed()
	{
		return false;
	}

	@Nullable
	@Override
	public DotNetTypeList getExtendList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		DotNetTypeRef[] extendTypeRefs = myClassEntry.getExtendTypeRefs();
		if(extendTypeRefs.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[extendTypeRefs.length];
		for(int i = 0; i < typeRefs.length; i++)
		{
			typeRefs[i] = new MsilToCSharpTypeRef(extendTypeRefs[i]);
		}
		return typeRefs;
	}

	@Override
	public boolean isInheritor(@NotNull DotNetTypeDeclaration other, boolean deep)
	{
		return DotNetInheritUtil.isInheritor(this, other, deep);
	}

	@Override
	public void processConstructors(@NotNull Processor<DotNetConstructorDeclaration> processor)
	{
		CSharpTypeDeclarationImplUtil.processConstructors(this, processor);
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
		return new DotNetGenericParameter[0];
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		DotNetNamedElement[] members = myClassEntry.getMembers();
		List<DotNetNamedElement> list = new ArrayList<DotNetNamedElement>(members.length);
		for(DotNetNamedElement member : members)
		{
			if(member instanceof MsilMethodEntry)
			{
				String name = member.getName();
				if(MsilHelper.STATIC_CONSTRUCTOR_NAME.equals(name))
				{
					//
				}
				else if(MsilHelper.CONSTRUCTOR_NAME.equals(name))
				{
					list.add(new MsilMethodAsCSharpConstructorDefinition(this, (MsilMethodEntry) member));
				}
				else
				{
					list.add(new MsilMethodAsCSharpMethodDefinition((MsilMethodEntry) member));
				}
			}
			else if(member instanceof MsilFieldEntry)
			{
				String name = member.getName();
				if(Comparing.equal(name, "value__") && isEnum())
				{
					continue;
				}

				list.add(new MsilFieldAsCSharpFieldDefinition((DotNetVariable) member));
			}
		}
		return list.isEmpty() ? DotNetNamedElement.EMPTY_ARRAY : list.toArray(new DotNetNamedElement[list.size()]);
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

	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return myClassEntry.getPresentableParentQName();
	}

	@Override
	public String getName()
	{
		return MsilHelper.cutGenericMarker(myClassEntry.getName());
	}

	@Nullable
	@Override
	public String getPresentableQName()
	{
		return MsilHelper.cutGenericMarker(myClassEntry.getPresentableQName());
	}

	@Override
	public String toString()
	{
		return myClassEntry.toString();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return myClassEntry.getNameIdentifier();
	}

	@Override
	public ItemPresentation getPresentation()
	{
		return ItemPresentationProviders.getItemPresentation(this);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}
}
