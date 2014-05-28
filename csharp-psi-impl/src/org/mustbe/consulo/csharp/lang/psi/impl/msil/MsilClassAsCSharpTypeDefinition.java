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
import java.util.Collections;
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
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.MsilHelper;
import org.mustbe.consulo.msil.lang.psi.MsilClassEntry;
import org.mustbe.consulo.msil.lang.psi.MsilEventEntry;
import org.mustbe.consulo.msil.lang.psi.MsilFieldEntry;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import org.mustbe.consulo.msil.lang.psi.MsilModifierList;
import org.mustbe.consulo.msil.lang.psi.MsilPropertyEntry;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import org.mustbe.consulo.msil.lang.psi.MsilXXXAcessor;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.ItemPresentationProviders;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
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
	private NotNullLazyValue<DotNetNamedElement[]> myMembersValue = new NotNullLazyValue<DotNetNamedElement[]>()
	{
		@NotNull
		@Override
		protected DotNetNamedElement[] compute()
		{
			DotNetNamedElement[] temp = myClassEntry.getMembers();
			List<DotNetNamedElement> copy = new ArrayList<DotNetNamedElement>(temp.length);
			Collections.addAll(copy, temp);

			List<DotNetNamedElement> list = new ArrayList<DotNetNamedElement>(temp.length);

			for(DotNetNamedElement element : temp)
			{
				if(element instanceof MsilPropertyEntry)
				{
					DotNetXXXAccessor[] accessors = ((MsilPropertyEntry) element).getAccessors();

					List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs = new ArrayList<Pair<DotNetXXXAccessor, MsilMethodEntry>>(2);

					for(DotNetXXXAccessor accessor : accessors)
					{
						MsilMethodEntry methodEntry = findMethodEntry(temp, (MsilXXXAcessor) accessor);
						if(methodEntry != null)
						{
							pairs.add(Pair.create(accessor, methodEntry));
							copy.remove(methodEntry);
						}
					}

					if(!pairs.isEmpty() && Comparing.equal(element.getName(), "Item"))
					{
						Pair<DotNetXXXAccessor, MsilMethodEntry> value = pairs.get(0);

						if(value.getFirst().getAccessorType() == MsilTokens._GET_KEYWORD && value.getSecond().getParameters().length == 1 ||
								value.getFirst().getAccessorType() == MsilTokens._SET_KEYWORD && value.getSecond().getParameters().length == 2)
						{
							list.add(new MsilPropertyAsCSharpArrayMethodDefinition((MsilPropertyEntry) element, pairs));
							continue;
						}
					}

					list.add(new MsilPropertyAsCSharpPropertyDefinition((MsilPropertyEntry) element, pairs));
				}
				else if(element instanceof MsilEventEntry)
				{
					DotNetXXXAccessor[] accessors = ((MsilEventEntry) element).getAccessors();

					List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs = new ArrayList<Pair<DotNetXXXAccessor, MsilMethodEntry>>(2);

					for(DotNetXXXAccessor accessor : accessors)
					{
						MsilMethodEntry methodEntry = findMethodEntry(temp, (MsilXXXAcessor) accessor);
						if(methodEntry != null)
						{
							pairs.add(Pair.create(accessor, methodEntry));
							copy.remove(methodEntry);
						}
					}
					list.add(new MsilEventAsCSharpEventDefinition((MsilEventEntry) element, pairs));
				}
				else if(element instanceof MsilFieldEntry)
				{
					String name = element.getName();
					if(Comparing.equal(name, "value__") && isEnum())
					{
						continue;
					}

					list.add(new MsilFieldAsCSharpFieldDefinition((DotNetVariable) element));
				}
			}

			for(DotNetNamedElement member : copy)
			{
				if(member instanceof MsilMethodEntry)
				{
					String name = member.getName();
					if(Comparing.equal(name, MsilHelper.STATIC_CONSTRUCTOR_NAME))
					{
						continue;
					}
					if(MsilHelper.CONSTRUCTOR_NAME.equals(name))
					{
						list.add(new MsilMethodAsCSharpConstructorDefinition(MsilClassAsCSharpTypeDefinition.this, (MsilMethodEntry) member));
					}
					else
					{
						list.add(new MsilMethodAsCSharpMethodDefinition(null, (MsilMethodEntry) member));
					}
				}
			}
			return list.isEmpty() ? DotNetNamedElement.EMPTY_ARRAY : list.toArray(new DotNetNamedElement[list.size()]);
		}

		private MsilMethodEntry findMethodEntry(DotNetNamedElement[] dotNetNamedElements, MsilXXXAcessor accessor)
		{
			for(DotNetNamedElement element : dotNetNamedElements)
			{
				if(element instanceof MsilMethodEntry && ((MsilMethodEntry) element).hasModifier(MsilTokens.SPECIALNAME_KEYWORD))
				{
					String originalMethodName = StringUtil.unquoteString(((MsilMethodEntry) element).getNameFromBytecode());
					if(Comparing.equal(originalMethodName, accessor.getMethodName()))
					{
						return (MsilMethodEntry) element;
					}
				}
			}
			return null;
		}
	};

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

	@Override
	public boolean isNested()
	{
		return myClassEntry.isNested();
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
		return myClassEntry.getGenericParameterList();
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myClassEntry.getGenericParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myClassEntry.getGenericParametersCount();
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return myMembersValue.getValue();
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
