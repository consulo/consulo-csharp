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

package consulo.csharp.lang.psi.impl.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.dotnet.psi.DotNetModifier;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpModifierListImpl extends CSharpElementImpl implements CSharpModifierList
{
	public CSharpModifierListImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitModifierList(this);
	}

	@NotNull
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
	public void addModifier(@NotNull DotNetModifier modifier)
	{
		CSharpModifierListImplUtil.addModifier(this, modifier);
	}

	@Override
	public void removeModifier(@NotNull DotNetModifier modifier)
	{
		CSharpModifierListImplUtil.removeModifier(this, modifier);
	}

	@NotNull
	@Override
	public CSharpModifier[] getModifiers()
	{
		return CSharpModifierListImplUtil.getModifiersCached(this).toArray(CSharpModifier.EMPTY_ARRAY);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return CSharpModifierListImplUtil.getModifiersCached(this).contains(CSharpModifier.as(modifier));
	}

	@Override
	public boolean hasModifierInTree(@NotNull DotNetModifier modifier)
	{
		IElementType iElementType = CSharpModifierListImplUtil.ourModifiers.get(CSharpModifier.as(modifier));
		return findChildByType(iElementType) != null;
	}

	@Nullable
	@Override
	public PsiElement getModifierElement(DotNetModifier modifier)
	{
		IElementType iElementType = CSharpModifierListImplUtil.ourModifiers.get(CSharpModifier.as(modifier));
		return findChildByType(iElementType);
	}

	@NotNull
	@Override
	public List<PsiElement> getModifierElements(@NotNull DotNetModifier modifier)
	{
		IElementType iElementType = CSharpModifierListImplUtil.ourModifiers.get(CSharpModifier.as(modifier));
		return findChildrenByType(iElementType);
	}

	@NotNull
	@Override
	public CSharpAttributeList[] getAttributeLists()
	{
		return findChildrenByClass(CSharpAttributeList.class);
	}
}