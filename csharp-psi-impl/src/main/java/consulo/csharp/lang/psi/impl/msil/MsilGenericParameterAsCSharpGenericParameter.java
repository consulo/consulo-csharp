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

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;

import javax.annotation.Nullable;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeListOwner;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class MsilGenericParameterAsCSharpGenericParameter extends MsilElementWrapper<DotNetGenericParameter> implements CSharpGenericParameter, DotNetAttributeListOwner
{
	private final NotNullLazyValue<DotNetTypeRef[]> myExtendTypeRefsValue;

	@RequiredReadAction
	public MsilGenericParameterAsCSharpGenericParameter(@Nonnull PsiElement parent, DotNetGenericParameter msilElement)
	{
		super(parent, msilElement);
		myExtendTypeRefsValue = NotNullLazyValue.createValue(() -> CSharpGenericConstraintUtil.getExtendTypes(this));
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		modifier = CSharpModifier.as(modifier);
		if(modifier == CSharpModifier.IN)
		{
			return myOriginal.hasModifier(DotNetModifier.CONTRAVARIANT);
		}
		else if(modifier == CSharpModifier.OUT)
		{
			return myOriginal.hasModifier(DotNetModifier.COVARIANT);
		}
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		if(another instanceof MsilGenericParameterAsCSharpGenericParameter)
		{
			return myOriginal.isEquivalentTo(((MsilGenericParameterAsCSharpGenericParameter) another).myOriginal);
		}
		return super.isEquivalentTo(another);
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameter(this);
	}

	@Override
	public String toString()
	{
		return "MsilGenericParameterAsCSharpGenericParameter: " + myOriginal.getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public int getIndex()
	{
		return myOriginal.getIndex();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		return myOriginal.getAttributes();
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return myExtendTypeRefsValue.getValue();
	}
}
