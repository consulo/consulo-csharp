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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeList;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpModifierListImpl extends CSharpElementImpl implements DotNetModifierList
{
	private static final Map<CSharpModifier, IElementType> ourModifiers = new LinkedHashMap<CSharpModifier, IElementType>()
	{
		{
			put(CSharpModifier.PUBLIC, CSharpTokens.PUBLIC_KEYWORD);
			put(CSharpModifier.PROTECTED, CSharpTokens.PROTECTED_KEYWORD);
			put(CSharpModifier.PRIVATE, CSharpTokens.PRIVATE_KEYWORD);
			put(CSharpModifier.STATIC, CSharpTokens.STATIC_KEYWORD);
			put(CSharpModifier.SEALED, CSharpTokens.SEALED_KEYWORD);
			put(CSharpModifier.ABSTRACT, CSharpTokens.ABSTRACT_KEYWORD);
			put(CSharpModifier.READONLY, CSharpTokens.READONLY_KEYWORD);
			put(CSharpModifier.UNSAFE, CSharpTokens.UNSAFE_KEYWORD);
			put(CSharpModifier.PARAMS, CSharpTokens.PARAMS_KEYWORD);
			put(CSharpModifier.THIS, CSharpTokens.THIS_KEYWORD);
			put(CSharpModifier.PARTIAL, CSharpSoftTokens.PARTIAL_KEYWORD);
			put(CSharpModifier.INTERNAL, CSharpSoftTokens.INTERNAL_KEYWORD);
			put(CSharpModifier.REF, CSharpSoftTokens.REF_KEYWORD);
			put(CSharpModifier.OUT, CSharpSoftTokens.OUT_KEYWORD);
			put(CSharpModifier.NEW, CSharpSoftTokens.NEW_KEYWORD);
		}
	};
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
		DotNetAttributeList[] childrenByClass = findChildrenByClass(DotNetAttributeList.class);
		if(childrenByClass.length == 0)
		{
			return DotNetAttribute.EMPTY_ARRAY;
		}
		List<DotNetAttribute> attributes = new ArrayList<DotNetAttribute>();
		for(DotNetAttributeList childrenByClas : childrenByClass)
		{
			Collections.addAll(attributes, childrenByClas.getAttributes());
		}
		return attributes.isEmpty() ? DotNetAttribute.EMPTY_ARRAY : attributes.toArray(new DotNetAttribute[attributes.size()]);
	}

	@NotNull
	@Override
	public CSharpModifier[] getModifiers()
	{
		List<CSharpModifier> list = new ArrayList<CSharpModifier>();
		for(CSharpModifier CSharpModifier : ourModifiers.keySet())
		{
			if(hasModifier(CSharpModifier))
			{
				list.add(CSharpModifier);
			}
		}
		return list.toArray(new CSharpModifier[list.size()]);
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		if(hasModifierInTree(modifier))
		{
			return true;
		}

		CSharpModifier cSharpModifier = CSharpModifier.as(modifier);
		PsiElement parent = getParent();
		switch(cSharpModifier)
		{
			case STATIC:
				if(parent instanceof CSharpFieldDeclaration)
				{
					if(((CSharpFieldDeclaration) parent).isConstant() && parent.getParent() instanceof CSharpTypeDeclaration)
					{
						return true;
					}
				}
				break;
		}
		return false;
	}

	@Override
	public boolean hasModifierInTree(@NotNull DotNetModifier modifier)
	{
		IElementType iElementType = ourModifiers.get(CSharpModifier.as(modifier));
		return iElementType != null && findChildByType(iElementType) != null;
	}

	@Nullable
	@Override
	public PsiElement getModifier(IElementType elementType)
	{
		return findChildByType(elementType);
	}
}
