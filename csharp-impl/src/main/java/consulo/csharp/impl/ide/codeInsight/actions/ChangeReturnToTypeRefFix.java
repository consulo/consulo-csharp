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

package consulo.csharp.impl.ide.codeInsight.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.codeEditor.Editor;
import consulo.component.util.localize.BundleBase;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class ChangeReturnToTypeRefFix implements SyntheticIntentionAction {
  private final SmartPsiElementPointer<DotNetLikeMethodDeclaration> myMethodPointer;
  @Nonnull
  private final DotNetTypeRef myToTypeRef;

  public ChangeReturnToTypeRefFix(@Nonnull DotNetLikeMethodDeclaration element, @Nonnull DotNetTypeRef toTypeRef) {
    myMethodPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
    myToTypeRef = toTypeRef;
  }

  @Nonnull
  @Override
  public String getText() {
    DotNetLikeMethodDeclaration element = myMethodPointer.getElement();
    if (element == null) {
      return "invalid";
    }
    return BundleBase.format("Make ''{0}'' return to ''{1}''", element.getName(), CSharpTypeRefPresentationUtil.buildTextWithKeyword
      (myToTypeRef));
  }

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    return myMethodPointer.getElement() != null;
  }

  @Override
  @RequiredReadAction
  public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiDocumentManager.getInstance(project).commitAllDocuments();

    DotNetLikeMethodDeclaration element = myMethodPointer.getElement();
    if (element == null) {
      return;
    }

    DotNetType typeOfVariable = element.getReturnType();
    if (typeOfVariable == null) {
      return;
    }
    String typeText = CSharpTypeRefPresentationUtil.buildShortText(myToTypeRef);

    DotNetType type = CSharpFileFactory.createMaybeStubType(project, typeText, typeOfVariable);

    typeOfVariable.replace(type);
  }
}