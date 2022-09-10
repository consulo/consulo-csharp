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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.impl.psi.stub.CSharpModifierListStub;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetModifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpStubModifierListImpl extends CSharpStubElementImpl<CSharpModifierListStub> implements CSharpModifierList
{
	public CSharpStubModifierListImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpStubModifierListImpl(@Nonnull CSharpModifierListStub stub, @Nonnull IStubElementType<? extends CSharpModifierListStub, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitModifierList(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		DotNetAttributeList[] attributeLists = getAttributeLists();
		if(attributeLists.length == 0)
		{
			return DotNetAttribute.EMPTY_ARRAY;
		}
		List<DotNetAttribute> attributes = new ArrayList<>();
		for(DotNetAttributeList attributeList : attributeLists)
		{
			Collections.addAll(attributes, attributeList.getAttributes());
		}
		return attributes.isEmpty() ? DotNetAttribute.EMPTY_ARRAY : attributes.toArray(new DotNetAttribute[attributes.size()]);
	}

	@Override
	public void addModifier(@Nonnull DotNetModifier modifier)
	{
		CSharpModifierListImplUtil.addModifier(this, modifier);
	}

	@Override
	public void removeModifier(@Nonnull DotNetModifier modifier)
	{
		CSharpModifierListImplUtil.removeModifier(this, modifier);
	}

	@Nonnull
	@Override
	public CSharpModifier[] getModifiers()
	{
		return CSharpModifierListImplUtil.getModifiersCached(this).toArray(CSharpModifier.EMPTY_ARRAY);
	}

	@Override
	public boolean hasModifier(@Nonnull DotNetModifier modifier)
	{
		return CSharpModifierListImplUtil.getModifiersCached(this).contains(CSharpModifier.as(modifier));
	}

	@Override
	public boolean hasModifierInTree(@Nonnull DotNetModifier modifier)
	{
		CSharpModifierListStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.hasModifier(modifier);
		}

		IElementType iElementType = CSharpModifierListImplUtil.asElementType(modifier);
		return findChildByType(iElementType) != null;
	}

	@Nullable
	@Override
	public PsiElement getModifierElement(DotNetModifier modifier)
	{
		IElementType iElementType = CSharpModifierListImplUtil.asElementType(modifier);
		return findChildByType(iElementType);
	}

	@Nonnull
	@Override
	public List<PsiElement> getModifierElements(@Nonnull DotNetModifier modifier)
	{
		IElementType iElementType = CSharpModifierListImplUtil.asElementType(modifier);
		return findChildrenByType(iElementType);
	}

	@Nonnull
	@Override
	public CSharpAttributeList[] getAttributeLists()
	{
		return getStubOrPsiChildren(CSharpStubElements.ATTRIBUTE_LIST, CSharpAttributeList.ARRAY_FACTORY);
	}
}
