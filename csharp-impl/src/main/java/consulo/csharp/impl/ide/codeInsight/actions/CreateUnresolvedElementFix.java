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
import consulo.annotation.access.RequiredWriteAction;
import consulo.application.Application;
import consulo.codeEditor.Editor;
import consulo.codeEditor.ScrollType;
import consulo.csharp.impl.ide.refactoring.CSharpGenerateUtil;
import consulo.csharp.lang.impl.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpBodyWithBraces;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetMemberOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.fileEditor.FileEditorManager;
import consulo.fileEditor.FileEditorProviderManager;
import consulo.ide.IdeBundle;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.intention.BaseIntentionAction;
import consulo.language.editor.intention.SyntheticIntentionAction;
import consulo.language.editor.template.Template;
import consulo.language.editor.template.TemplateManager;
import consulo.language.psi.*;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.OpenFileDescriptor;
import consulo.navigation.OpenFileDescriptorFactory;
import consulo.project.Project;
import consulo.ui.ex.awt.Messages;
import consulo.util.collection.ArrayUtil;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public abstract class CreateUnresolvedElementFix extends BaseIntentionAction implements SyntheticIntentionAction {
  protected final SmartPsiElementPointer<CSharpReferenceExpression> myPointer;
  protected final String myReferenceName;

  public CreateUnresolvedElementFix(CSharpReferenceExpression expression) {
    myPointer = SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
    myReferenceName = expression.getReferenceName();
  }

  protected abstract CreateUnresolvedElementFixContext createGenerateContext();

  @Override
  public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file) {
    return createGenerateContext() != null;
  }

  @Nonnull
  @Override
  public abstract String getText();

  @RequiredReadAction
  public abstract void buildTemplate(@Nonnull CreateUnresolvedElementFixContext context,
                                     CSharpContextUtil.ContextType contextType,
                                     @Nonnull PsiFile file,
                                     @Nonnull Template template);

  @Nonnull
  public PsiElement getElementForAfterAdd(@Nonnull DotNetNamedElement[] elements, @Nonnull CSharpBodyWithBraces targetForGenerate) {
    return ArrayUtil.getLastElement(elements);
  }

  @Override
  @RequiredWriteAction
  public void invoke(@Nonnull Project project, final Editor editor, PsiFile file) throws IncorrectOperationException {
    PsiDocumentManager.getInstance(project).commitAllDocuments();
    final CreateUnresolvedElementFixContext generateContext = createGenerateContext();
    if (generateContext == null) {
      return;
    }

    CSharpContextUtil.ContextType contextType = CSharpContextUtil.getParentContextTypeForReference(generateContext.getExpression());

    final TemplateManager templateManager = TemplateManager.getInstance(project);
    final Template template = templateManager.createTemplate("", "");
    template.setToReformat(true);

    template.addTextSegment("\n\n");
    buildTemplate(generateContext, contextType, file, template);

    new WriteCommandAction.Simple<Object>(project, file) {
      @Override
      protected void run() throws Throwable {
        DotNetMemberOwner targetForGenerate = generateContext.getTargetForGenerate();

        assert targetForGenerate instanceof CSharpBodyWithBraces;

        Editor editorForAdd;
        DotNetNamedElement[] members = targetForGenerate.getMembers();
        if (members.length == 0) {
          CSharpGenerateUtil.normalizeBraces((CSharpBodyWithBraces)targetForGenerate);

          PsiElement leftBrace = ((CSharpBodyWithBraces)targetForGenerate).getLeftBrace();

          editorForAdd = openEditor(leftBrace, leftBrace.getTextOffset() + 1);
        }
        else {
          PsiElement lastElement = getElementForAfterAdd(members, (CSharpBodyWithBraces)targetForGenerate);

          editorForAdd = openEditor(lastElement, lastElement.getTextRange().getEndOffset());
        }

        if (editorForAdd == null) {
          return;
        }

        templateManager.startTemplate(editorForAdd, template);
      }
    }.execute();
  }

  @Nullable
  protected Editor openEditor(@Nonnull PsiElement anchor, int offset) {
    PsiFile containingFile = anchor.getContainingFile();
    if (containingFile == null) {
      return null;
    }
    VirtualFile virtualFile = containingFile.getVirtualFile();
    if (virtualFile == null) {
      return null;
    }

    Project project = containingFile.getProject();
    FileEditorProviderManager editorProviderManager = FileEditorProviderManager.getInstance();
    if (editorProviderManager.getProviders(project, virtualFile).length == 0) {
      Messages.showMessageDialog(project,
                                 IdeBundle.message("error.files.of.this.type.cannot.be.opened", Application.get().getName().get()),
                                 IdeBundle.message("title.cannot.open.file"),
                                 Messages.getErrorIcon());
      return null;
    }

    OpenFileDescriptor descriptor = OpenFileDescriptorFactory.getInstance(project).builder(virtualFile).build();
    Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
    if (editor != null) {
      editor.getCaretModel().moveToOffset(offset);
      editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
      return editor;
    }
    return null;
  }
}
