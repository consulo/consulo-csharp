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

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class AddXModifierFix extends PsiElementBaseIntentionAction
{
	private CSharpModifier[] myModifiers;

	public AddXModifierFix(CSharpModifier... modifiers)
	{
		myModifiers = modifiers;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		DotNetModifierListOwner owner = CSharpIntentionUtil.findOwner(element);
		if(owner == null || !owner.isWritable())
		{
			return;
		}

		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		beforeAdd(modifierList);

		for(CSharpModifier modifier : ArrayUtil.reverseArray(myModifiers))
		{
			modifierList.addModifier(modifier);
		}
	}

	protected void beforeAdd(DotNetModifierList modifierList)
	{

	}

	public boolean isAllow(DotNetModifierListOwner owner, CSharpModifier[] modifier)
	{
		return true;
	}

	@Override
	@RequiredDispatchThread
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		DotNetModifierListOwner owner = CSharpIntentionUtil.findOwner(element);
		return owner != null && !hasModifiers(owner) && isAllow(owner, myModifiers) && owner.isWritable();
	}

	@RequiredReadAction
	protected boolean hasModifiers(DotNetModifierListOwner owner)
	{
		for(CSharpModifier modifier : myModifiers)
		{
			if(!owner.hasModifier(modifier))
			{
				return false;
			}
		}
		return true;
	}

	@NotNull
	@Override
	public String getText()
	{
		return "Make " + StringUtil.join(myModifiers, new Function<CSharpModifier, String>()
		{
			@Override
			public String fun(CSharpModifier modifier)
			{
				return modifier.getPresentableText();
			}
		}, " ");
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
