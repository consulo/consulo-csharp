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

package consulo.csharp.ide.highlight.quickFix;

import org.jetbrains.annotations.NotNull;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
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
public class RenameQuickFix extends BaseIntentionAction
{
	private final String myNewName;
	private final SmartPsiElementPointer<PsiNamedElement> myPointer;

	public RenameQuickFix(@NotNull String newName, @NotNull PsiNamedElement namedElement)
	{
		myNewName = newName;
		myPointer = SmartPointerManager.getInstance(namedElement.getProject()).createSmartPsiElementPointer(namedElement);
		setText("Rename '" + namedElement.getName() + "' to '" + myNewName + "'");
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
}
