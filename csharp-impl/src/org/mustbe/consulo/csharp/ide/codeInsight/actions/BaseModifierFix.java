package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

	public abstract boolean isValidCondition(@NotNull DotNetModifierList modifierList, @NotNull DotNetModifier modifier);

	@NotNull
	public abstract String getActionName();

	public abstract void doAction(@NotNull DotNetModifierList modifierList, @NotNull DotNetModifier modifier);

	@NotNull
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

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile)
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
	public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException
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
