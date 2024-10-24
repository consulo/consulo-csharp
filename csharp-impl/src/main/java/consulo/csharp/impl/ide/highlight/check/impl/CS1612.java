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
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.*;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dataContext.DataManager;
import consulo.dotnet.psi.*;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.editor.refactoring.rename.RenameElementAction;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.action.AnAction;
import consulo.ui.ex.action.AnActionEvent;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 01-Nov-17
 */
public class CS1612 extends CompilerCheck<CSharpAssignmentExpressionImpl> {
  public static class IntroduceTempVariableFix extends BaseIntentionAction implements SyntheticIntentionAction {
    private final SmartPsiElementPointer<DotNetExpression> myQualifierPointer;
    private final SmartPsiElementPointer<CSharpFieldDeclaration> myFieldPointer;
    private final SmartPsiElementPointer<DotNetExpression> myValueExpression;
    private final String myOperatorText;

    public IntroduceTempVariableFix(DotNetExpression qualifier,
                                    CSharpFieldDeclaration field,
                                    DotNetExpression rightExpression,
                                    String operatorText) {
      myOperatorText = operatorText;
      SmartPointerManager manager = SmartPointerManager.getInstance(qualifier.getProject());

      myQualifierPointer = manager.createSmartPsiElementPointer(qualifier);
      myFieldPointer = manager.createSmartPsiElementPointer(field);
      myValueExpression = manager.createSmartPsiElementPointer(rightExpression);
    }

    @Nonnull
    @Override
    public String getText() {
      return "Introduce temp variable";
    }

    @Override
    @RequiredUIAccess
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
      CSharpFieldDeclaration element = myFieldPointer.getElement();

      DotNetExpression value = myValueExpression.getElement();

      DotNetExpression left = myQualifierPointer.getElement();

      DotNetVariable targetSet = null;
      if (left instanceof CSharpReferenceExpression) {
        PsiElement targetSetElement = ((CSharpReferenceExpression)left).resolve();
        if (targetSetElement instanceof DotNetVariable && isWritable(element)) {
          targetSet = (DotNetVariable)targetSetElement;
        }
      }

      return targetSet != null && element != null && value != null;
    }

    @RequiredReadAction
    private static boolean isWritable(DotNetVariable targetSetElement) {
      if (targetSetElement.isConstant()) {
        return false;
      }

      if (targetSetElement.hasModifier(CSharpModifier.READONLY)) {
        return false;
      }

      if (targetSetElement instanceof CSharpPropertyDeclaration) {
        DotNetXAccessor[] accessors = ((CSharpPropertyDeclaration)targetSetElement).getAccessors();
        for (DotNetXAccessor accessor : accessors) {
          if (accessor.getAccessorKind() == DotNetXAccessor.Kind.SET) {
            return true;
          }
        }

        return false;
      }
      return true;
    }

    @Override
    @RequiredUIAccess
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      CSharpFieldDeclaration element = myFieldPointer.getElement();

      DotNetExpression value = myValueExpression.getElement();

      DotNetExpression left = myQualifierPointer.getElement();

      DotNetVariable targetSet = null;
      if (left instanceof CSharpReferenceExpression) {
        PsiElement targetSetElement = ((CSharpReferenceExpression)left).resolve();
        if (targetSetElement instanceof DotNetVariable && isWritable((DotNetVariable)targetSetElement)) {
          targetSet = (DotNetVariable)targetSetElement;
        }
      }

      if (targetSet == null || element == null || value == null) {
        return;
      }

      Collection<String> suggestedVariableNames = CSharpNameSuggesterUtil.getSuggestedNames(targetSet.toTypeRef(true), value);

      StringBuilder builder = new StringBuilder();
      String varName = ContainerUtil.getFirstItem(suggestedVariableNames);
      builder.append(CSharpTypeRefPresentationUtil.buildShortText(targetSet.toTypeRef(true))).append(" ").append(varName).append(" = ");
      builder.append("new ").append(CSharpTypeRefPresentationUtil.buildShortText(targetSet.toTypeRef(true))).append("();\n");

      builder.append(varName)
             .append(".")
             .append(element.getName())
             .append(" ")
             .append(myOperatorText)
             .append(" ")
             .append(value.getText())
             .append(";\n");

      builder.append(left.getText()).append(" = ").append(varName).append(";");


      PsiElement parent = value.getParent().getParent();

      DotNetLikeMethodDeclaration method = CSharpFileFactory.createMethod(project, "void test() { " + builder + "}");

      DotNetCodeBodyProxy proxy = method.getCodeBlock();

      CSharpBlockStatementImpl codeBlock = (CSharpBlockStatementImpl)proxy.getElement();

      assert codeBlock != null;

      DotNetStatement[] statements = codeBlock.getStatements();

      PsiElement assignToField = parent.replace(statements[2]);

      PsiElement assign = assignToField.getParent().addBefore(statements[1], assignToField);

      // var declaration
      PsiElement first = assignToField.getParent().addBefore(statements[0], assign);

      first.add(PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n"));
      assign.add(PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n"));

      PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

      if (first instanceof CSharpLocalVariableDeclarationStatement) {
        CSharpLocalVariable[] variables = ((CSharpLocalVariableDeclarationStatement)first).getVariables();
        if (variables.length > 0) {
          SwingUtilities.invokeLater(() ->
                                     {
                                       editor.getCaretModel().moveToOffset(variables[0].getTextOffset());

                                       // reset all under caret highlight due it will provide some bug after caret change
                                       consulo.ide.impl.idea.codeInsight.daemon.impl.IdentifierHighlighterPass.clearMyHighlights(editor.getDocument(),
                                                                                                                                 project);

                                       AnAction action = new RenameElementAction();
                                       AnActionEvent event = AnActionEvent.createFromAnAction(action,
                                                                                              null,
                                                                                              "",
                                                                                              DataManager.getInstance()
                                                                                                         .getDataContext(editor.getComponent()));
                                       action.actionPerformed(event);
                                     });
        }
      }
    }
  }

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull CSharpAssignmentExpressionImpl element) {
    DotNetExpression leftExpression = element.getLeftExpression();

    DotNetExpression rightExpression = element.getRightExpression();
    if (rightExpression == null) {
      return null;
    }

    String operatorText = element.getOperatorElement().getText();

    if (leftExpression instanceof CSharpReferenceExpression) {
      PsiElement targetField = ((CSharpReferenceExpression)leftExpression).resolve();

      if (targetField instanceof CSharpFieldDeclaration) {
        PsiElement parent = targetField.getParent();
        if (!(parent instanceof CSharpTypeDeclaration)) {
          return null;
        }

        if (!(((CSharpTypeDeclaration)parent).isStruct())) {
          return null;
        }

        DotNetExpression qualifier = ((CSharpReferenceExpression)leftExpression).getQualifier();
        if (qualifier instanceof CSharpReferenceExpression) {
          DotNetExpression nextQualifier = ((CSharpReferenceExpression)qualifier).getQualifier();
          if (nextQualifier != null) {
            if (nextQualifier instanceof CSharpReferenceExpression) {
              if ((((CSharpReferenceExpression)nextQualifier).kind() == CSharpReferenceExpression.ResolveToKind.THIS || ((CSharpReferenceExpression)nextQualifier)
                .kind() ==
                CSharpReferenceExpression.ResolveToKind.BASE)) {
                return null;
              }
            }

            PsiElement qualifierNext = ((CSharpReferenceExpression)qualifier).resolve();
            if (qualifierNext instanceof CSharpPropertyDeclaration) {
              return newBuilder(qualifier, formatElement(qualifierNext)).withQuickFix(new IntroduceTempVariableFix(qualifier,
                                                                                                                   (CSharpFieldDeclaration)targetField,
                                                                                                                   rightExpression,
                                                                                                                   operatorText));
            }
          }
        }
        else {
          return null;
        }
      }

    }
    return null;
  }
}
