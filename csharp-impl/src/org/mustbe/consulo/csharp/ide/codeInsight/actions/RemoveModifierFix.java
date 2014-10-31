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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class RemoveModifierFix extends BaseIntentionAction
{
	private final DotNetModifier[] myModifiers;
	private final DotNetModifierListOwner myModifierElementOwner;

	public RemoveModifierFix(DotNetModifier[] modifiers, DotNetModifierListOwner parent)
	{
		myModifiers = modifiers;
		myModifierElementOwner = parent;
	}

	public RemoveModifierFix(DotNetModifier modifier, DotNetModifierListOwner parent)
	{
		this(new DotNetModifier[]{modifier}, parent);
	}

	@NotNull
	private PsiElement[] findModifiers()
	{
		DotNetModifierList modifierList = myModifierElementOwner.getModifierList();
		if(modifierList == null)
		{
			return PsiElement.EMPTY_ARRAY;
		}
		List<PsiElement> elements = new SmartList<PsiElement>();
		for(DotNetModifier modifier : myModifiers)
		{
			PsiElement modifierElement = modifierList.getModifierElement(modifier);
			if(modifierElement != null)
			{
				elements.add(modifierElement);
			}
		}
		return ContainerUtil.toArray(elements, PsiElement.ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public String getText()
	{
		if(myModifiers.length == 1)
		{
			return "Remove '" + myModifiers[0].getPresentableText() + "' modifier";
		}
		else
		{
			return "Remove " + StringUtil.join(myModifiers, new Function<DotNetModifier, String>()
			{
				@Override
				public String fun(DotNetModifier modifier)
				{
					return "'" + modifier.getPresentableText() + "'";
				}
			}, " & ") + " modifiers";
		}
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile)
	{
		return findModifiers().length != 0;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException
	{
		DotNetModifierList modifierList = myModifierElementOwner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		for(DotNetModifier modifier : myModifiers)
		{
			modifierList.removeModifier(modifier);
		}
	}
}
