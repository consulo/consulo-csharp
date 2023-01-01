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
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.ArrayUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 20.01.15
 */
public class CS1722 extends CompilerCheck<DotNetTypeList> {
  public static class MoveToFirstPositionFix extends BaseIntentionAction implements SyntheticIntentionAction {
    private SmartPsiElementPointer<DotNetType> myTypePointer;

    public MoveToFirstPositionFix(DotNetType baseType) {
      myTypePointer = SmartPointerManager.getInstance(baseType.getProject()).createSmartPsiElementPointer(baseType);
      setText("Place base type at first");
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
      return myTypePointer.getElement() != null;
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      DotNetType element = myTypePointer.getElement();
      if (element == null) {
        return;
      }

      DotNetTypeList parent = (DotNetTypeList)element.getParent();

      DotNetType[] types = parent.getTypes();

      int i = ArrayUtil.indexOf(types, element);
      if (i <= 0) {
        return;
      }
      DotNetType elementAtZeroPosition = types[0];

      PsiElement baseElementCopy = element.copy();
      PsiElement elementAtZeroCopy = elementAtZeroPosition.copy();

      elementAtZeroPosition.replace(baseElementCopy);
      element.replace(elementAtZeroCopy);
    }
  }

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull DotNetTypeList element) {
    if (element.getNode().getElementType() != CSharpStubElements.EXTENDS_LIST) {
      return null;
    }

    CSharpTypeDeclaration resolvedElement = null;
    DotNetType baseType = null;
    DotNetType[] types = element.getTypes();

    for (DotNetType type : types) {
      DotNetTypeRef typeRef = type.toTypeRef();
      PsiElement temp = typeRef.resolve().getElement();
      if (temp instanceof CSharpTypeDeclaration && !((CSharpTypeDeclaration)temp).isInterface()) {
        resolvedElement = (CSharpTypeDeclaration)temp;
        baseType = type;
        break;
      }
    }

    if (baseType == null) {
      return null;
    }
    int i = ArrayUtil.indexOf(types, baseType);
    if (i != 0) {
      CSharpTypeDeclaration parent = (CSharpTypeDeclaration)element.getParent();
      return newBuilder(baseType, formatElement(parent), formatElement(resolvedElement)).withQuickFix(new MoveToFirstPositionFix(baseType));
    }
    return null;
  }
}
