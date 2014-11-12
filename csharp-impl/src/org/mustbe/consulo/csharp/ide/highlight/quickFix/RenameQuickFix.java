package org.mustbe.consulo.csharp.ide.highlight.quickFix;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 12.11.14
 */
public class RenameQuickFix implements IntentionAction
{
	private final String myNewName;
	private final SmartPsiElementPointer<PsiNamedElement> myPointer;

	public RenameQuickFix(@NotNull String newName, @NotNull PsiNamedElement namedElement)
	{
		myNewName = newName;
		myPointer = SmartPointerManager.getInstance(namedElement.getProject()).createSmartPsiElementPointer(namedElement);
	}

	@NotNull
	@Override
	public String getText()
	{
		PsiNamedElement element = myPointer.getElement();
		if(element == null)
		{
			throw new IllegalArgumentException();
		}
		return "Rename '" + element.getName() + "' to '" + myNewName + "'";
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
	{
		return myPointer.getElement() != null;
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
	{
		PsiNamedElement element = myPointer.getElement();
		if(element == null)
		{
			return;
		}
		element.setName(myNewName);
	}

	@Override
	public boolean startInWriteAction()
	{
		return true;
	}
}
