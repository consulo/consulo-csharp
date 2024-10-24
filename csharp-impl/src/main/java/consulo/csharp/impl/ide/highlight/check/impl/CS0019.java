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
import consulo.annotation.access.RequiredWriteAction;
import consulo.codeEditor.Editor;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOperatorReferenceImpl;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.MultiMap;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 13.04.2016
 */
public class CS0019 extends CompilerCheck<CSharpBinaryExpressionImpl> {
  public static class ReplaceByEqualsCallFix extends BaseIntentionAction implements SyntheticIntentionAction {
    private SmartPsiElementPointer<CSharpBinaryExpressionImpl> myElementPointer;

    public ReplaceByEqualsCallFix(CSharpBinaryExpressionImpl element) {
      myElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
      setText("Replace by 'Equals()' call");
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
      return myElementPointer.getElement() != null;
    }

    @Override
    @RequiredWriteAction
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      CSharpBinaryExpressionImpl element = myElementPointer.getElement();
      if (element == null) {
        return;
      }

      DotNetExpression leftExpression = element.getLeftExpression();
      DotNetExpression rightExpression = element.getRightExpression();
      if (leftExpression == null || rightExpression == null) {
        return;
      }

      StringBuilder builder = new StringBuilder();
      if (element.getOperatorElement().getOperatorElementType() == CSharpTokens.NTEQ) {
        builder.append("!");
      }
      builder.append(leftExpression.getText());
      builder.append(".Equals(");
      builder.append(rightExpression.getText());
      builder.append(")");

      DotNetExpression expression = CSharpFileFactory.createExpression(project, builder.toString());

      element.replace(expression);
    }
  }

  private static MultiMap<String, String> ourAllowedMap = new MultiMap<String, String>();

  static {
    String[] values = {
      DotNetTypes.System.SByte,
      DotNetTypes.System.Byte,
      DotNetTypes.System.Int16,
      DotNetTypes.System.UInt16,
      DotNetTypes.System.Int32,
      DotNetTypes.System.UInt32,
      DotNetTypes.System.Int64,
      DotNetTypes.System.UInt64
    };

    for (String value : values) {
      ourAllowedMap.put(value, Arrays.asList(values));
    }
  }

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull CSharpBinaryExpressionImpl element) {
    CSharpOperatorReferenceImpl operatorElement = element.getOperatorElement();
    IElementType operatorElementType = operatorElement.getOperatorElementType();
    if (operatorElementType == CSharpTokens.EQEQ || operatorElementType == CSharpTokens.NTEQ) {
      DotNetExpression leftExpression = element.getLeftExpression();
      DotNetExpression rightExpression = element.getRightExpression();
      if (leftExpression == null || rightExpression == null) {
        return null;
      }

      DotNetTypeRef leftType = leftExpression.toTypeRef(true);
      DotNetTypeRef rightType = rightExpression.toTypeRef(true);

      if (leftType == DotNetTypeRef.UNKNOWN_TYPE || rightType == DotNetTypeRef.UNKNOWN_TYPE) {
        return null;
      }

      boolean applicable = CSharpTypeUtil.isInheritableWithImplicit(leftType,
                                                                    rightType,
                                                                    element.getResolveScope()) || CSharpTypeUtil.isInheritableWithImplicit(
        rightType,
        leftType,
        element.getResolveScope());

      if (!applicable) {
        PsiElement leftElement = leftType.resolve().getElement();
        if (leftElement instanceof DotNetGenericParameter && CSharpGenericConstraintUtil.findGenericConstraint((DotNetGenericParameter)leftElement) == null && rightType instanceof
          CSharpNullTypeRef) {
          return null;
        }

        Pair<String, DotNetTypeDeclaration> leftPair = CSharpTypeUtil.resolveTypeElement(leftType);
        if (leftPair != null) {
          Collection<String> allowedSetLeft = ourAllowedMap.get(leftPair.getFirst());

          Pair<String, DotNetTypeDeclaration> rightPair = CSharpTypeUtil.resolveTypeElement(rightType);
          if (rightPair != null && allowedSetLeft.contains(rightPair.getFirst())) {
            return null;
          }
        }

        return newBuilder(operatorElement,
                          operatorElement.getCanonicalText(),
                          formatTypeRef(leftType),
                          formatTypeRef(rightType)).withQuickFix(new ReplaceByEqualsCallFix
                                                                   (element));
      }
    }
    return null;
  }
}
