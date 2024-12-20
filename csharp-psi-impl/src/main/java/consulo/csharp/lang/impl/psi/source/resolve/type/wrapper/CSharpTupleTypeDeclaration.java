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

package consulo.csharp.lang.impl.psi.source.resolve.type.wrapper;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.ToNativeElementTransformers;
import consulo.csharp.lang.impl.psi.light.builder.CSharpLightFieldDeclarationBuilder;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNameIdentifierOwner;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.Navigatable;
import consulo.util.collection.ArrayUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleTypeDeclaration extends LightElement implements CSharpTypeDeclaration
{
	public static final Key<PsiNameIdentifierOwner> TUPLE_ELEMENT = Key.create("tuple.element");

	private final CSharpTypeDeclaration myType;
	private final DotNetNamedElement[] myMembers;

	@RequiredReadAction
	public CSharpTupleTypeDeclaration(DotNetTypeDeclaration type, PsiNameIdentifierOwner[] variables, DotNetTypeRef[] typeRefs)
	{
		super(type.getManager(), CSharpLanguage.INSTANCE);

		myType = (CSharpTypeDeclaration) type;

		final DotNetNamedElement[] members = type.getMembers();
		DotNetNamedElement[] targetMembers = members;
		for(int i = 0; i < variables.length; i++)
		{
			PsiNameIdentifierOwner variable = variables[i];
			String name = variable.getName();
			if(name == null)
			{
				continue;
			}
			CSharpLightFieldDeclarationBuilder builder = new CSharpLightFieldDeclarationBuilder(type.getProject())
			{
				@Override
				public void navigate(boolean requestFocus)
				{
					PsiElement element = getUserData(TUPLE_ELEMENT);
					if(element instanceof Navigatable)
					{
						((Navigatable) element).navigate(true);
					}
				}

				@Override
				public boolean isEquivalentTo(PsiElement another)
				{
					if(another == getUserData(TUPLE_ELEMENT))
					{
						return true;
					}
					return super.isEquivalentTo(another);
				}

				@Override
				public PsiFile getContainingFile()
				{
					return getUserData(TUPLE_ELEMENT).getContainingFile();
				}
			};
			builder.putUserData(TUPLE_ELEMENT, variable);
			builder.withParent(this);
			builder.withTypeRef(typeRefs[i]);
			builder.addModifier(CSharpModifier.PUBLIC);
			builder.withName(name);
			String real = "Item" + (i + 1);

			DotNetNamedElement element = ContainerUtil.find(members, it -> real.equals(it.getName()));
			if(element != null)
			{
				builder.setNavigationElement(ToNativeElementTransformers.transform(element));
			}

			targetMembers = ArrayUtil.append(targetMembers, builder, DotNetNamedElement[]::new);
		}
		myMembers = targetMembers;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return myType.isEquivalentTo(another);
	}

	@Override
	public boolean isInterface()
	{
		return myType.isInterface();
	}

	@Override
	public boolean isStruct()
	{
		return myType.isStruct();
	}

	@Override
	public boolean isEnum()
	{
		return myType.isEnum();
	}

	@Override
	public boolean isNested()
	{
		return myType.isNested();
	}

	@Override
	@Nullable
	public DotNetTypeList getExtendList()
	{
		return myType.getExtendList();
	}

	@Override
	@RequiredReadAction
	@Nonnull
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return myType.getExtendTypeRefs();
	}

	@Override
	@RequiredReadAction
	public boolean isInheritor(@Nonnull String s, boolean b)
	{
		return myType.isInheritor(s, b);
	}

	@Override
	@RequiredReadAction
	@Nonnull
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return myType.getTypeRefForEnumConstants();
	}

	@Override
	@RequiredReadAction
	@Nullable
	public String getVmQName()
	{
		return myType.getVmQName();
	}

	@Override
	@RequiredReadAction
	@Nullable
	public String getVmName()
	{
		return myType.getVmName();
	}

	@Override
	@RequiredReadAction
	@Nullable
	public String getPresentableParentQName()
	{
		return myType.getPresentableParentQName();
	}

	@Override
	@RequiredReadAction
	@Nullable
	public String getPresentableQName()
	{
		return myType.getPresentableQName();
	}

	@Override
	@NonNls
	public String toString()
	{
		return myType.toString();
	}

	@Override
	@RequiredWriteAction
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		return myType.setName(s);
	}

	@Override
	@RequiredReadAction
	public boolean hasModifier(@Nonnull DotNetModifier dotNetModifier)
	{
		return myType.hasModifier(dotNetModifier);
	}

	@Override
	@RequiredReadAction
	@Nullable
	public DotNetModifierList getModifierList()
	{
		return myType.getModifierList();
	}

	@Override
	@Nullable
	public DotNetGenericParameterList getGenericParameterList()
	{
		return myType.getGenericParameterList();
	}

	@Override
	@Nonnull
	public DotNetGenericParameter[] getGenericParameters()
	{
		return myType.getGenericParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		return myType.getGenericParametersCount();
	}

	@Override
	@RequiredReadAction
	@Nullable
	public PsiElement getNameIdentifier()
	{
		return myType.getNameIdentifier();
	}

	@Override
	@RequiredReadAction
	@Nonnull
	public DotNetNamedElement[] getMembers()
	{
		return myMembers;
	}

	@Override
	@Nullable
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return myType.getGenericConstraintList();
	}

	@Override
	@Nonnull
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return myType.getGenericConstraints();
	}

	@Override
	@RequiredReadAction
	public PsiElement getLeftBrace()
	{
		return myType.getLeftBrace();
	}

	@Override
	@RequiredReadAction
	public PsiElement getRightBrace()
	{
		return myType.getRightBrace();
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return myType.getName();
	}
}
