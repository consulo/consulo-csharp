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
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.source.CSharpAsExpressionImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import org.jetbrains.annotations.Nls;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.02.2015
 */
public class CS0077 extends CompilerCheck<CSharpAsExpressionImpl> {
  public static class AddQuestMarkQuickFix implements SyntheticIntentionAction {
    private SmartPsiElementPointer<DotNetType> myPointer;

    public AddQuestMarkQuickFix(DotNetType type) {
      myPointer = SmartPointerManager.getInstance(type.getProject()).createSmartPsiElementPointer(type);
    }

    @Nls
    @Nonnull
    @Override
    public String getText() {
      return "Add '?'";
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

      DotNetType type = CSharpFileFactory.createMaybeStubType(project, element.getText() + "?", element);
      element.replace(type);
    }
  }

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull CSharpAsExpressionImpl element) {
    DotNetTypeRef typeRef = element.toTypeRef(false);
    if (typeRef == DotNetTypeRef.ERROR_TYPE) {
      return null;
    }

    if (!typeRef.resolve().isNullable()) {
      DotNetType type = element.getType();
      assert type != null;
      return newBuilder(element.getAsKeyword(), "as", formatTypeRef(typeRef)).withQuickFix(new AddQuestMarkQuickFix(type));
    }
    return super.checkImpl(languageVersion, highlightContext, element);
  }
}
