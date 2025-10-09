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

import consulo.application.Result;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetExpression;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class ConvertToNormalCallFix extends PsiElementBaseIntentionAction implements SyntheticIntentionAction {
  public static ConvertToNormalCallFix INSTANCE = new ConvertToNormalCallFix();

  public ConvertToNormalCallFix() {
    setText(LocalizeValue.localizeTODO("Convert to normal call"));
  }

  @Override
  public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException {
    final CSharpMethodCallExpressionImpl callExpression = PsiTreeUtil.getParentOfType(element, CSharpMethodCallExpressionImpl.class);
    assert callExpression != null;
    CSharpReferenceExpression referenceExpression = (CSharpReferenceExpression)callExpression.getCallExpression();

    PsiElement qualifier = referenceExpression.getQualifier();
    assert qualifier != null;

    PsiElement resolve = referenceExpression.resolve();
    if (!CSharpMethodImplUtil.isExtensionWrapper(resolve)) {
      return;
    }

    final StringBuilder builder = new StringBuilder();

    CSharpTypeDeclaration parentTypeOfMethod = (CSharpTypeDeclaration)resolve.getParent();

    CSharpTypeDeclaration parentTypeOfExpression = PsiTreeUtil.getParentOfType(element, CSharpTypeDeclaration.class);

    if (parentTypeOfMethod != parentTypeOfExpression) {
      builder.append(parentTypeOfMethod.getName()).append(".");
    }

    builder.append(referenceExpression.getReferenceName());
    builder.append("(");

    DotNetExpression[] parameterExpressions = callExpression.getParameterExpressions();
    List<PsiElement> elements = new ArrayList<PsiElement>(parameterExpressions.length + 1);
    elements.add(qualifier);
    Collections.addAll(elements, parameterExpressions);

    builder.append(StringUtil.join(elements, element1 -> element1.getText(), ", "));
    builder.append(")");

    new WriteCommandAction<Object>(project, getText().get(), element.getContainingFile()) {
      @Override
      protected void run(Result<Object> objectResult) throws Throwable {
        CSharpExpressionStatementImpl statement = (CSharpExpressionStatementImpl)CSharpFileFactory.createStatement(callExpression
                                                                                                                     .getProject(),
                                                                                                                   builder.toString());

        callExpression.replace(statement.getExpression());
      }
    }.execute();
  }

  @Override
  public boolean isAvailable(
    @Nonnull Project project, Editor editor, @Nonnull PsiElement element) {
    return true;
  }
}
