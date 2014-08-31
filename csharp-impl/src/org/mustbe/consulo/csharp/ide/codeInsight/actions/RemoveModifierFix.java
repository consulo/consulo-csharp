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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 10.06.14
 */
public class RemoveModifierFix extends BaseIntentionAction
{
	private final DotNetModifier myModifier;
	private final DotNetModifierListOwner myModifierElementOwner;

	public RemoveModifierFix(DotNetModifier modifier, DotNetModifierListOwner parent)
	{
		myModifier = modifier;
		myModifierElementOwner = parent;
	}

	@Nullable
	private PsiElement findModifier()
	{
		DotNetModifierList modifierList = myModifierElementOwner.getModifierList();
		if(modifierList == null)
		{
			return null;
		}
		return modifierList.getModifierElement(myModifier);
	}

	@NotNull
	@Override
	public String getText()
	{
		return "Remove '" + myModifier.getPresentableText() + "' modifier";
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
		return findModifier() != null;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException
	{
		DotNetModifierList modifierList = myModifierElementOwner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		modifierList.removeModifier(myModifier);
	}
}
