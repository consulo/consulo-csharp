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
import consulo.csharp.impl.ide.codeInsight.actions.RemoveModifierFix;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpCodeBodyProxy;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetCodeBlockOwner;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.Nls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 18.11.14
 */
public class CS0500 extends CompilerCheck<CSharpMethodDeclaration> {
  public static class RemoveCodeBlockFix implements SyntheticIntentionAction {
    private SmartPsiElementPointer<DotNetCodeBlockOwner> myPointer;

    public RemoveCodeBlockFix(DotNetCodeBlockOwner declaration) {
      myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
    }

    @Nonnull
    @Override
    public LocalizeValue getText() {
      return LocalizeValue.localizeTODO("Remove code block");
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
      return myPointer.getElement() != null;
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      DotNetCodeBlockOwner element = myPointer.getElement();
      if (element == null) {
        return;
      }
      PsiDocumentManager.getInstance(project).commitAllDocuments();

      CSharpCodeBodyProxy codeBlock = (CSharpCodeBodyProxy)element.getCodeBlock();

      codeBlock.replaceBySemicolon();
    }
  }

  @RequiredReadAction
  @Nullable
  @Override
  public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull CSharpMethodDeclaration element) {
    PsiElement nameIdentifier = element.getNameIdentifier();
    if (nameIdentifier == null) {
      return null;
    }
    if ((element.hasModifier(CSharpModifier.ABSTRACT) || element.isDelegate()) && element.getCodeBlock().isNotSemicolonAndNotEmpty()) {
      CompilerCheckBuilder compilerCheckBuilder = newBuilder(nameIdentifier, formatElement(element));
      compilerCheckBuilder.withQuickFix(new RemoveCodeBlockFix(element));
      if (element.hasModifier(CSharpModifier.ABSTRACT)) {
        compilerCheckBuilder.withQuickFix(new RemoveModifierFix(CSharpModifier.ABSTRACT, element));
      }
      return compilerCheckBuilder;
    }
    return null;
  }
}
