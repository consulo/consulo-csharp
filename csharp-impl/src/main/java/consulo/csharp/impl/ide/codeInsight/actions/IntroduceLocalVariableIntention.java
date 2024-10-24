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

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.refactoring.introduceVariable.CSharpIntroduceLocalVariableHandler;
import consulo.csharp.lang.impl.psi.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionStatementImpl;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.editor.CodeInsightBundle;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.refactoring.action.BaseRefactoringIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SyntheticElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;

import jakarta.annotation.Nonnull;

/**
 * @author michael.shumenko
 * @since Aug 09, 2015
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "csharp.introduce.local.var", categories = "C#", fileExtensions = "cs")
public class IntroduceLocalVariableIntention extends BaseRefactoringIntentionAction {
  @Override
  public void invoke(@Nonnull Project project,
                     Editor editor,
                     @Nonnull PsiElement element) throws IncorrectOperationException {
    new CSharpIntroduceLocalVariableHandler().invoke(project, editor, element.getContainingFile(), null);
  }

  @Override
  @RequiredUIAccess
  public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement psi) {
    CSharpExpressionStatementImpl exprStmt = PsiTreeUtil.getParentOfType(psi, CSharpExpressionStatementImpl.class);
    if (psi instanceof SyntheticElement || exprStmt == null) {
      return false;
    }

    DotNetExpression expression = exprStmt.getExpression();
    if (expression instanceof CSharpAssignmentExpressionImpl) {
      return false;
    }

    DotNetTypeRef ref = expression.toTypeRef(true);
    return !(ref == DotNetTypeRef.ERROR_TYPE || DotNetTypeRefUtil.isVmQNameEqual(ref, DotNetTypes.System.Void));
  }

  @Nonnull
  @Override
  public String getText() {
    return CodeInsightBundle.message("intention.introduce.variable.text");
  }
}
