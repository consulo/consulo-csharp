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

package consulo.csharp.ide.codeInsight.actions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public abstract class BaseModifierFix extends BaseIntentionAction
{
	private final DotNetModifier[] myModifiers;
	private final SmartPsiElementPointer<DotNetModifierListOwner> myModifierElementOwnerPointer;

	public BaseModifierFix(DotNetModifier[] modifiers, DotNetModifierListOwner parent)
	{
		myModifiers = modifiers;
		myModifierElementOwnerPointer = SmartPointerManager.getInstance(parent.getProject()).createSmartPsiElementPointer(parent);
	}

	public BaseModifierFix(DotNetModifier modifier, DotNetModifierListOwner parent)
	{
		this(new DotNetModifier[]{modifier}, parent);
	}

	public abstract boolean isValidCondition(@Nonnull DotNetModifierList modifierList, @Nonnull DotNetModifier modifier);

	@Nonnull
	public abstract String getActionName();

	public abstract void doAction(@Nonnull DotNetModifierList modifierList, @Nonnull DotNetModifier modifier);

	@Nonnull
	@Override
	public String getText()
	{
		if(myModifiers.length == 1)
		{
			return getActionName() + " '" + myModifiers[0].getPresentableText() + "' modifier";
		}
		else
		{
			return getActionName() + " " + StringUtil.join(myModifiers, new Function<DotNetModifier, String>()
			{
				@Override
				public String fun(DotNetModifier modifier)
				{
					return "'" + modifier.getPresentableText() + "'";
				}
			}, " & ") + " modifiers";
		}
	}

	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile psiFile)
	{
		DotNetModifierList modifierList = getModifierList();
		if(modifierList == null)
		{
			return false;
		}

		for(DotNetModifier modifier : myModifiers)
		{
			if(isValidCondition(modifierList, modifier))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void invoke(@Nonnull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException
	{
		DotNetModifierList modifierList = getModifierList();
		if(modifierList == null)
		{
			return;
		}

		for(DotNetModifier modifier : myModifiers)
		{
			doAction(modifierList, modifier);
		}
	}

	@Nullable
	private DotNetModifierList getModifierList()
	{
		DotNetModifierListOwner element = myModifierElementOwnerPointer.getElement();
		if(element == null)
		{
			return null;
		}
		return element.getModifierList();
	}
}
