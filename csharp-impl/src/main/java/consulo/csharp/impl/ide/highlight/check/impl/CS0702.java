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
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.util.lang.Pair;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 04.12.14
 */
public class CS0702 extends CompilerCheck<CSharpGenericConstraintTypeValue> {
  public static class ReplaceConstraintFix implements SyntheticIntentionAction {
    private final String myKeywordForReplace;
    private final SmartPsiElementPointer<CSharpGenericConstraintTypeValue> myPointer;

    public ReplaceConstraintFix(CSharpGenericConstraintTypeValue declaration, String keywordForReplace) {
      myKeywordForReplace = keywordForReplace;
      myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
    }

    @Nonnull
    @Override
    public LocalizeValue getText() {
      return LocalizeValue.localizeTODO("Replace by '" + myKeywordForReplace + "' constraint");
    }

    @Override
    public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
      return myPointer.getElement() != null;
    }

    @Override
    public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
      CSharpGenericConstraintTypeValue element = myPointer.getElement();
      if (element == null) {
        return;
      }
      PsiDocumentManager.getInstance(project).commitAllDocuments();

      CSharpMethodDeclaration method = (CSharpMethodDeclaration)CSharpFileFactory.createMethod(project, "void test<T> where T : " +
        myKeywordForReplace + " {}");

      CSharpGenericConstraint newGenericConstraint = method.getGenericConstraints()[0];

      element.replace(newGenericConstraint.getGenericConstraintValues()[0]);
    }
  }

  private static final Map<String, String> ourErrorMap = new HashMap<String, String>() {
    {
      put(DotNetTypes.System.Object, "class");
      put(DotNetTypes.System.ValueType, "struct");
    }
  };

  @RequiredReadAction
  @Nullable
  @Override
  public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion,
                                        @Nonnull CSharpHighlightContext highlightContext,
                                        @Nonnull CSharpGenericConstraintTypeValue element) {
    DotNetTypeRef typeRef = element.toTypeRef();
    Pair<String, DotNetTypeDeclaration> pair = CSharpTypeUtil.resolveTypeElement(typeRef);
    if (pair == null) {
      return null;
    }
    String keywordForReplace = ourErrorMap.get(pair.getFirst());
    if (keywordForReplace != null) {
      DotNetType type = element.getType();
      assert type != null;
      return newBuilder(type, pair.getFirst()).withQuickFix(new ReplaceConstraintFix(element, keywordForReplace));
    }
    return null;
  }
}
