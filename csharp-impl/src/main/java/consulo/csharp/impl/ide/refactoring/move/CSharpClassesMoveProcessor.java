/*
 * Copyright 2013-2019 consulo.io
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
package consulo.csharp.impl.ide.refactoring.move;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.dotnet.module.DotNetNamespaceGeneratePolicy;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.editor.refactoring.BaseRefactoringProcessor;
import consulo.language.editor.refactoring.event.RefactoringElementListener;
import consulo.language.editor.refactoring.event.RefactoringEventData;
import consulo.language.editor.refactoring.localize.RefactoringLocalize;
import consulo.language.editor.refactoring.move.FileReferenceContextUtil;
import consulo.language.editor.refactoring.move.MoveCallback;
import consulo.language.editor.refactoring.move.MoveFileHandler;
import consulo.language.editor.refactoring.move.fileOrDirectory.MoveFilesOrDirectoriesDialog;
import consulo.language.editor.refactoring.move.fileOrDirectory.MoveFilesOrDirectoriesUtil;
import consulo.language.editor.refactoring.rename.RenameUtil;
import consulo.language.editor.refactoring.util.CommonRefactoringUtil;
import consulo.language.editor.util.EditorHelper;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiReference;
import consulo.language.psi.path.FileReference;
import consulo.language.psi.path.PsiDynaReference;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.psi.search.ReferencesSearch;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.localize.LocalizeValue;
import consulo.logging.Logger;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.ui.ex.awt.Messages;
import consulo.ui.ex.awt.UIUtil;
import consulo.usage.NonCodeUsageInfo;
import consulo.usage.UsageInfo;
import consulo.usage.UsageViewDescriptor;
import consulo.util.lang.Couple;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * @author VISTALL
 * @since 2019-07-20
 */
public class CSharpClassesMoveProcessor extends BaseRefactoringProcessor {
    public static class MyUsageInfo extends UsageInfo {
        Couple<DotNetNamedElement> myDeclarationAndResolveTargetCouple;
        PsiReference myReference;

        @RequiredReadAction
        public MyUsageInfo(PsiElement element, Couple<DotNetNamedElement> couple, PsiReference reference) {
            super(element);
            myDeclarationAndResolveTargetCouple = couple;
            myReference = reference;
        }
    }

    private static final Logger LOG = Logger.getInstance(CSharpClassesMoveProcessor.class);

    protected final PsiElement[] myElementsToMove;
    protected final boolean mySearchForReferences;
    protected final boolean mySearchInComments;
    protected final boolean mySearchInNonJavaFiles;
    private final PsiDirectory myNewParent;
    private final MoveCallback myMoveCallback;
    private NonCodeUsageInfo[] myNonCodeUsages;

    public CSharpClassesMoveProcessor(
        @Nonnull Project project,
        PsiElement[] elements,
        PsiDirectory newParent,
        boolean searchInComments,
        boolean searchInNonJavaFiles,
        MoveCallback moveCallback,
        Runnable prepareSuccessfulCallback
    ) {
        this(project, elements, newParent, true, searchInComments, searchInNonJavaFiles, moveCallback, prepareSuccessfulCallback);
    }

    public CSharpClassesMoveProcessor(
        @Nonnull Project project,
        PsiElement[] elements,
        PsiDirectory newParent,
        boolean searchForReferences,
        boolean searchInComments,
        boolean searchInNonJavaFiles,
        MoveCallback moveCallback,
        Runnable prepareSuccessfulCallback
    ) {
        super(project, prepareSuccessfulCallback);
        myElementsToMove = elements;
        myNewParent = newParent;
        mySearchForReferences = searchForReferences;
        mySearchInComments = searchInComments;
        mySearchInNonJavaFiles = searchInNonJavaFiles;
        myMoveCallback = moveCallback;
    }

    @Override
    @Nonnull
    protected UsageViewDescriptor createUsageViewDescriptor(@Nonnull UsageInfo[] usages) {
        return new CSharpClassesViewDescriptor(myElementsToMove, myNewParent);
    }

    @Override
    @Nonnull
    @RequiredReadAction
    protected UsageInfo[] findUsages() {
        List<UsageInfo> result = new ArrayList<>();
        List<Couple<DotNetNamedElement>> children = new ArrayList<>();
        for (PsiElement psiElement : myElementsToMove) {
            if (psiElement instanceof CSharpFile) {
                children.addAll(CSharpMoveClassesUtil.findTypesAndNamespaces(psiElement));
            }
        }

        for (Couple<DotNetNamedElement> couple : children) {
            DotNetNamedElement second = couple.getSecond();

            for (PsiReference reference : ReferencesSearch.search(second, mapScope(second))) {
                result.add(new MyUsageInfo(reference.getElement(), couple, reference));
            }
        }

        return result.toArray(new UsageInfo[result.size()]);
    }


    @Nonnull
    @RequiredReadAction
    public static GlobalSearchScope mapScope(DotNetNamedElement element) {
        if (element instanceof DotNetNamespaceAsElement) {
            return GlobalSearchScope.projectScope(element.getProject());
        }
        return element.getResolveScope();
    }

    @Override
    protected void refreshElements(@Nonnull PsiElement[] elements) {
        LOG.assertTrue(elements.length == myElementsToMove.length);
        System.arraycopy(elements, 0, myElementsToMove, 0, elements.length);
    }

    @Override
    @RequiredWriteAction
    protected void performPsiSpoilingRefactoring() {
        if (myNonCodeUsages != null) {
            RenameUtil.renameNonCodeUsages(myProject, myNonCodeUsages);
        }
    }

    @Override
    @RequiredWriteAction
    protected void performRefactoring(@Nonnull UsageInfo[] usages) {
        try {
            List<PsiFile> movedFiles = new ArrayList<>();
            Map<PsiElement, PsiElement> oldToNewMap = new HashMap<>();
            for (PsiElement element : myElementsToMove) {
                RefactoringElementListener elementListener = getTransaction().getElementListener(element);

                if (element instanceof PsiDirectory movedDir) {
                    if (mySearchForReferences) {
                        encodeDirectoryFiles(element);
                    }
                    MoveFilesOrDirectoriesUtil.doMoveDirectory(movedDir, myNewParent);
                    for (PsiElement psiElement : element.getChildren()) {
                        processDirectoryFiles(movedFiles, oldToNewMap, psiElement);
                    }
                }
                else if (element instanceof PsiFile movedFile) {
                    if (mySearchForReferences) {
                        FileReferenceContextUtil.encodeFileReferences(element);
                    }

                    MoveFileHandler.forElement(movedFile).prepareMovedFile(movedFile, myNewParent, oldToNewMap);

                    PsiFile moving = myNewParent.findFile(movedFile.getName());
                    if (moving == null) {
                        MoveFilesOrDirectoriesUtil.doMoveFile(movedFile, myNewParent);
                    }
                    moving = myNewParent.findFile(movedFile.getName());
                    movedFiles.add(moving);
                }

                elementListener.elementMoved(element);
            }

            notifyNamespaces();

            // sort by offset descending to process correctly several usages in one PsiElement [IDEADEV-33013]
            CommonRefactoringUtil.sortDepthFirstRightLeftOrder(usages);

            DumbService.getInstance(myProject).completeJustSubmittedTasks();

            // fix references in moved files to outer files
            for (PsiFile movedFile : movedFiles) {
                MoveFileHandler.forElement(movedFile).updateMovedFile(movedFile);
                if (mySearchForReferences) {
                    FileReferenceContextUtil.decodeFileReferences(movedFile);
                }
            }

            myNonCodeUsages = retargetUsages(usages);

            if (MoveFilesOrDirectoriesDialog.isOpenInEditor()) {
                EditorHelper.openFilesInEditor(movedFiles.toArray(new PsiFile[movedFiles.size()]));
            }

            if (myMoveCallback != null) {
                myMoveCallback.refactoringCompleted();
            }
        }
        catch (IncorrectOperationException e) {
            if (e.getCause() instanceof IOException ioe) {
                LOG.info(e);
                myProject.getApplication().invokeLater(
                    () -> Messages.showMessageDialog(
                        myProject,
                        ioe.getMessage(),
                        RefactoringLocalize.errorTitle().get(),
                        UIUtil.getErrorIcon()
                    )
                );
            }
            else {
                LOG.error(e);
            }
        }
    }

    @RequiredWriteAction
    private void notifyNamespaces() {
        for (PsiElement psiElement : myElementsToMove) {
            if (psiElement instanceof CSharpFile) {
                Set<Couple<DotNetNamedElement>> typesAndNamespaces = CSharpMoveClassesUtil.findTypesAndNamespaces(psiElement);

                for (Couple<DotNetNamedElement> couple : typesAndNamespaces) {
                    if (couple.getFirst() instanceof CSharpNamespaceDeclaration namespaceDeclaration) {
                        notifyNamespaceChange(namespaceDeclaration);
                    }
                }
            }
        }
    }

    @RequiredWriteAction
    private void notifyNamespaceChange(CSharpNamespaceDeclaration declaration) {
        PsiElement parent = declaration.getParent();
        if (!(parent instanceof CSharpFile)) {
            return;
        }

        DotNetSimpleModuleExtension extension = ModuleUtilCore.getExtension(declaration, DotNetSimpleModuleExtension.class);
        if (extension == null) {
            return;
        }

        DotNetNamespaceGeneratePolicy namespaceGeneratePolicy = extension.getNamespaceGeneratePolicy();

        String expectedNamespace = namespaceGeneratePolicy.calculateNamespace(declaration.getContainingFile().getContainingDirectory());
        if (expectedNamespace == null) {
            return;
        }

        declaration.setNamespace(expectedNamespace);
    }

    @Nullable
    @Override
    protected String getRefactoringId() {
        return "refactoring.move";
    }

    @Nullable
    @Override
    protected RefactoringEventData getBeforeData() {
        RefactoringEventData data = new RefactoringEventData();
        data.addElements(myElementsToMove);
        return data;
    }

    @Nullable
    @Override
    protected RefactoringEventData getAfterData(@Nonnull UsageInfo[] usages) {
        RefactoringEventData data = new RefactoringEventData();
        data.addElement(myNewParent);
        return data;
    }

    @RequiredReadAction
    private static void encodeDirectoryFiles(PsiElement psiElement) {
        if (psiElement instanceof PsiFile) {
            FileReferenceContextUtil.encodeFileReferences(psiElement);
        }
        else if (psiElement instanceof PsiDirectory) {
            for (PsiElement element : psiElement.getChildren()) {
                encodeDirectoryFiles(element);
            }
        }
    }

    @RequiredWriteAction
    private static void processDirectoryFiles(List<PsiFile> movedFiles, Map<PsiElement, PsiElement> oldToNewMap, PsiElement psiElement) {
        if (psiElement instanceof PsiFile) {
            PsiFile movedFile = (PsiFile) psiElement;
            MoveFileHandler.forElement(movedFile).prepareMovedFile(movedFile, movedFile.getParent(), oldToNewMap);
            movedFiles.add(movedFile);
        }
        else if (psiElement instanceof PsiDirectory) {
            for (PsiElement element : psiElement.getChildren()) {
                processDirectoryFiles(movedFiles, oldToNewMap, element);
            }
        }
    }

    @RequiredWriteAction
    public static NonCodeUsageInfo[] retargetUsages(UsageInfo[] usages) {
        List<NonCodeUsageInfo> nonCodeUsages = new ArrayList<>();
        for (UsageInfo usageInfo : usages) {
            if (usageInfo instanceof MyUsageInfo) {
                MyUsageInfo info = (MyUsageInfo) usageInfo;

                DotNetNamedElement declaration = info.myDeclarationAndResolveTargetCouple.getFirst();

                if (info.getReference() instanceof FileReference || info.getReference() instanceof PsiDynaReference) {
                    PsiElement usageElement = info.getElement();
                    if (usageElement != null) {
                        PsiFile usageFile = usageElement.getContainingFile();
                        PsiFile psiFile = usageFile.getViewProvider().getPsi(usageFile.getViewProvider().getBaseLanguage());
                        if (psiFile != null && psiFile.equals(declaration.getContainingFile())) {
                            continue;  // already processed in MoveFilesOrDirectoriesUtil.doMoveFile
                        }
                    }
                }
                PsiElement refElement = info.myReference.getElement();
                if (refElement != null && refElement.isValid()) {
                    info.myReference.bindToElement(info.myDeclarationAndResolveTargetCouple.getFirst());
                }
            }
            else if (usageInfo instanceof NonCodeUsageInfo) {
                nonCodeUsages.add((NonCodeUsageInfo) usageInfo);
            }
        }

        return nonCodeUsages.toArray(new NonCodeUsageInfo[nonCodeUsages.size()]);
    }

    @Nonnull
    @Override
    protected LocalizeValue getCommandName() {
        return RefactoringLocalize.moveTitle();
    }
}
