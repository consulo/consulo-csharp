/*
 * Copyright 2013-2015 must-be.org
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
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeList;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifierList;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiParserFacade;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 15.11.2015
 */
public class CSharpAnonymousModifierListImpl extends LightElement implements CSharpModifierList
{
	private CSharpAnonymousMethodExpression myMethodExpression;

	protected CSharpAnonymousModifierListImpl(CSharpAnonymousMethodExpression methodExpression)
	{
		super(methodExpression.getManager(), methodExpression.getLanguage());
		myMethodExpression = methodExpression;
	}

	@Override
	public String toString()
	{
		return "CSharpAnonymousModifierListImpl: " + myMethodExpression.getClass().getSimpleName();
	}

	@Override
	public void addModifier(@NotNull DotNetModifier m)
	{
		CSharpModifier modifier = CSharpModifier.as(m);
		PsiElement anchor = myMethodExpression.getFirstChild();

		CSharpFieldDeclaration field = CSharpFileFactory.createField(myMethodExpression.getProject(), modifier.getPresentableText() + " int b");
		PsiElement modifierElement = field.getModifierList().getModifierElement(modifier);
		assert modifierElement != null;
		PsiElement psiElement = myMethodExpression.addBefore(modifierElement, anchor);
		myMethodExpression.addAfter(PsiParserFacade.SERVICE.getInstance(myMethodExpression.getProject()).createWhiteSpaceFromText(" "), psiElement);
	}

	@Override
	public void removeModifier(@NotNull DotNetModifier dotNetModifier)
	{
		CSharpModifierListImplUtil.removeModifier(this, dotNetModifier);
	}

	@NotNull
	@Override
	public DotNetModifier[] getModifiers()
	{
		List<CSharpModifier> list = new ArrayList<CSharpModifier>();
		for(CSharpModifier modifier : CSharpModifier.values())
		{
			if(hasModifier(modifier))
			{
				list.add(modifier);
			}
		}
		return list.toArray(new CSharpModifier[list.size()]);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		return DotNetAttribute.EMPTY_ARRAY;
	}

	@Override
	public boolean hasModifier(@NotNull DotNetModifier dotNetModifier)
	{
		return getModifierElement(dotNetModifier) != null;
	}

	@Override
	public boolean hasModifierInTree(@NotNull DotNetModifier dotNetModifier)
	{
		return getModifierElement(dotNetModifier) != null;
	}

	@Nullable
	@Override
	public PsiElement getModifierElement(DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		switch(as)
		{
			case ASYNC:
				ASTNode node = myMethodExpression.getNode().findChildByType(CSharpSoftTokens.ASYNC_KEYWORD);
				return node != null ? node.getPsi() : null;
		}
		return null;
	}

	@NotNull
	@Override
	public List<PsiElement> getModifierElements(@NotNull DotNetModifier dotNetModifier)
	{
		PsiElement modifierElement = getModifierElement(dotNetModifier);
		if(modifierElement == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(modifierElement);
	}

	@NotNull
	@Override
	public CSharpAttributeList[] getAttributeLists()
	{
		return CSharpAttributeList.EMPTY_ARRAY;
	}
}
