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
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.util.NotNullLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.util.IncorrectOperationException;

import org.jspecify.annotations.Nullable;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class MsilGenericParameterAsCSharpGenericParameter extends MsilElementWrapper<DotNetGenericParameter> implements CSharpGenericParameter, DotNetAttributeListOwner
{
	private final NotNullLazyValue<DotNetTypeRef[]> myExtendTypeRefsValue;

	@RequiredReadAction
	public MsilGenericParameterAsCSharpGenericParameter(PsiElement parent, DotNetGenericParameter msilElement)
	{
		super(parent, msilElement);
		myExtendTypeRefsValue = NotNullLazyValue.createValue(() -> CSharpGenericConstraintUtil.getExtendTypes(this));
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(DotNetModifier modifier)
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
	public void accept(CSharpElementVisitor visitor)
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
	public PsiElement setName(String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public int getIndex()
	{
		return myOriginal.getIndex();
	}

	@RequiredReadAction
	@Override
	public DotNetAttribute[] getAttributes()
	{
		return myOriginal.getAttributes();
	}

	@RequiredReadAction
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return myExtendTypeRefsValue.getValue();
	}
}
