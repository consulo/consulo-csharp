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
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class AddXModifierFix extends PsiElementBaseIntentionAction
{
	private CSharpModifier myModifier;

	public AddXModifierFix(CSharpModifier modifier)
	{
		myModifier = modifier;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		DotNetModifierListOwner owner = findOwner(element);
		if(owner == null)
		{
			return;
		}

		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		beforeAdd(modifierList);

		modifierList.addModifier(myModifier);
	}

	protected void beforeAdd(DotNetModifierList modifierList)
	{

	}

	public boolean isAllow(DotNetModifierListOwner owner, CSharpModifier modifier)
	{
		return true;
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		DotNetModifierListOwner owner = findOwner(element);
		return owner != null && !owner.hasModifier(myModifier) && isAllow(owner, myModifier);
	}

	@Nullable
	public DotNetModifierListOwner findOwner(@NotNull PsiElement element)
	{
		if(element.getParent() instanceof DotNetModifierList)
		{
			return (DotNetModifierListOwner) element.getParent().getParent();
		}
		if(element.getParent() instanceof DotNetModifierListOwner)
		{
			return (DotNetModifierListOwner) element.getParent();
		}
		return null;
	}

	@NotNull
	@Override
	public String getText()
	{
		return "Make " + myModifier.getPresentableText();
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
