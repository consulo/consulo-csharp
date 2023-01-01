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
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFactory;
import consulo.csharp.lang.impl.psi.fragment.CSharpFragmentFileImpl;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 14.12.14
 */
public class ReplaceTypeQuickFix extends BaseIntentionAction implements SyntheticIntentionAction {
  private final SmartPsiElementPointer<DotNetType> myPointer;
  private final String myTypeText;

  public ReplaceTypeQuickFix(@Nonnull DotNetType type, @Nonnull DotNetTypeRef typeRef) {
    myPointer = SmartPointerManager.getInstance(type.getProject()).createSmartPsiElementPointer(type);

    myTypeText = CSharpTypeRefPresentationUtil.buildShortText(typeRef);
    setText("Replace '" + type.getText() + "' by '" + myTypeText + "'");
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    return myPointer.getElement() != null;
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    DotNetType element = myPointer.getElement();
    if (element == null) {
      return;
    }

    CSharpFragmentFileImpl typeFragment = CSharpFragmentFactory.createTypeFragment(project, myTypeText, element);

    DotNetType newType = PsiTreeUtil.getChildOfType(typeFragment, DotNetType.class);
    if (newType == null) {
      return;
    }

    element.replace(newType);
  }
}
