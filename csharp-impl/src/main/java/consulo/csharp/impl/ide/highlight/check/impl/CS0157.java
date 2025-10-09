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

package consulo.csharp.impl.ide.highlight.check.impl;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.source.CSharpFinallyStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpReturnStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 01.01.15
 */
public class CS0157 extends CompilerCheck<CSharpReturnStatementImpl> {
  public static class RemoveReturnStatementFix implements SyntheticIntentionAction {
    private SmartPsiElementPointer<CSharpReturnStatementImpl> myPointer;

    public RemoveReturnStatementFix(CSharpReturnStatementImpl declaration) {
      myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
    }

    @Nonnull
    @Override
    public LocalizeValue getText() {
      return LocalizeValue.localizeTODO("Remove return statement");
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
      return myPointer.getElement() != null;
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      CSharpReturnStatementImpl element = myPointer.getElement();
      if (element == null) {
        return;
      }
      element.delete();
    }
  }

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull CSharpReturnStatementImpl element) {
    CSharpFinallyStatementImpl finallyStatement = PsiTreeUtil.getParentOfType(element, CSharpFinallyStatementImpl.class);
    if (finallyStatement != null) {
      return newBuilder(element).withQuickFix(new RemoveReturnStatementFix(element));
    }
    return null;
  }
}
