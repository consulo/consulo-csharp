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

package consulo.csharp.impl.ide.highlight.quickFix;

import consulo.codeEditor.Editor;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiNamedElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 12.11.14
 */
public class RenameQuickFix extends BaseIntentionAction implements SyntheticIntentionAction {
  private final String myNewName;
  private final SmartPsiElementPointer<PsiNamedElement> myPointer;

  public RenameQuickFix(@Nonnull String newName, @Nonnull PsiNamedElement namedElement) {
    myNewName = newName;
    myPointer = SmartPointerManager.getInstance(namedElement.getProject()).createSmartPsiElementPointer(namedElement);
    setText("Rename '" + namedElement.getName() + "' to '" + myNewName + "'");
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    return myPointer.getElement() != null;
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiNamedElement element = myPointer.getElement();
    if (element == null) {
      return;
    }
    element.setName(myNewName);
  }
}
