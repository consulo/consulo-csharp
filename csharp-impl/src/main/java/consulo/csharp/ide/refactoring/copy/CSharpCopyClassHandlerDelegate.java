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

package consulo.csharp.ide.refactoring.copy;

import com.intellij.CommonBundle;
import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.util.EditorHelper;
import com.intellij.ide.util.PlatformPackageUtil;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.copy.CopyFilesOrDirectoriesDialog;
import com.intellij.refactoring.copy.CopyFilesOrDirectoriesHandler;
import com.intellij.refactoring.copy.CopyHandler;
import com.intellij.refactoring.copy.CopyHandlerDelegateBase;
import com.intellij.refactoring.move.moveFilesOrDirectories.MoveFilesOrDirectoriesUtil;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetNamedElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.10.2015
 */
public class CSharpCopyClassHandlerDelegate extends CopyHandlerDelegateBase
{
	private static Logger LOG = Logger.getInstance(CSharpCopyClassHandlerDelegate.class);

	@Override
	public boolean canCopy(PsiElement[] elements, boolean fromUpdate)
	{
		if(elements.length == 1 && elements[0] instanceof CSharpTypeDeclaration)
		{
			return true;
		}
		return false;
	}

	@Override
	@RequiredReadAction
	public void doCopy(final PsiElement[] elements, PsiDirectory defaultTargetDirectory)
	{
		CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) elements[0];

		if(defaultTargetDirectory == null)
		{
			defaultTargetDirectory = PlatformPackageUtil.getDirectory(typeDeclaration.getContainingFile());
		}

		Project project = defaultTargetDirectory != null ? defaultTargetDirectory.getProject() : typeDeclaration.getProject();
		if(defaultTargetDirectory != null)
		{
			defaultTargetDirectory = CopyFilesOrDirectoriesHandler.resolveDirectory(defaultTargetDirectory);
			if(defaultTargetDirectory == null)
			{
				return;
			}
		}

		defaultTargetDirectory = tryNotNullizeDirectory(project, defaultTargetDirectory);

		doCopy(typeDeclaration, defaultTargetDirectory, project);
	}

	@RequiredReadAction
	private static void doCopy(@Nonnull CSharpTypeDeclaration typeDeclaration, @Nullable PsiDirectory defaultTargetDirectory, Project project)
	{
		PsiDirectory targetDirectory;
		String newName;
		boolean openInEditor;
		VirtualFile virtualFile = PsiUtilCore.getVirtualFile(typeDeclaration);
		CopyFilesOrDirectoriesDialog dialog = new CopyFilesOrDirectoriesDialog(new PsiElement[]{typeDeclaration.getContainingFile()}, defaultTargetDirectory, project, false);
		if(dialog.showAndGet())
		{
			newName = dialog.getNewName();
			targetDirectory = dialog.getTargetDirectory();
			openInEditor = dialog.openInEditor();
		}
		else
		{
			return;
		}

		if(targetDirectory != null)
		{
			PsiManager manager = PsiManager.getInstance(project);
			try
			{
				if(virtualFile.isDirectory())
				{
					PsiFileSystemItem psiElement = manager.findDirectory(virtualFile);
					MoveFilesOrDirectoriesUtil.checkIfMoveIntoSelf(psiElement, targetDirectory);
				}
			}
			catch(IncorrectOperationException e)
			{
				CommonRefactoringUtil.showErrorHint(project, null, e.getMessage(), CommonBundle.getErrorTitle(), null);
				return;
			}

			CommandProcessor.getInstance().executeCommand(project, () -> doCopy(typeDeclaration, newName, targetDirectory, false, openInEditor), RefactoringBundle.message("copy.handler.copy.files" +
					".directories"), null);
		}
	}

	@Override
	@RequiredReadAction
	public void doClone(PsiElement element)
	{
		PsiDirectory targetDirectory;
		if(element instanceof PsiDirectory)
		{
			targetDirectory = ((PsiDirectory) element).getParentDirectory();
		}
		else
		{
			targetDirectory = PlatformPackageUtil.getDirectory(element);
		}
		targetDirectory = tryNotNullizeDirectory(element.getProject(), targetDirectory);
		if(targetDirectory == null)
		{
			return;
		}

		CopyFilesOrDirectoriesDialog dialog = new CopyFilesOrDirectoriesDialog(new PsiElement[]{element.getContainingFile()}, null, element.getProject(), true);
		if(dialog.showAndGet())
		{
			String newName = dialog.getNewName();
			doCopy((CSharpTypeDeclaration) element, newName, targetDirectory, true, true);
		}
	}

	@RequiredReadAction
	private static void doCopy(@Nonnull final CSharpTypeDeclaration target,
							   @Nullable final String newName,
							   @Nonnull final PsiDirectory targetDirectory,
							   final boolean doClone,
							   final boolean openInEditor)
	{
		Project project = targetDirectory.getProject();

		PsiFile oldFile = targetDirectory.findFile(newName);
		if(oldFile != null)
		{
			Messages.showErrorDialog(project, "File already exists", RefactoringBundle.message("error.title"));
			return;
		}

		PsiFile containingFile = target.getContainingFile();

		PsiFile copyFile = (PsiFile) containingFile.copy();
		copyFile.setName(newName);

		CSharpTypeDeclaration copy = PsiTreeUtil.findElementOfClassAtOffset(copyFile, target.getTextOffset(), CSharpTypeDeclaration.class, false);

		assert copy != null;

		String oldTypeName = target.getName();
		String copyTypeName = FileUtil.getNameWithoutExtension(newName);
		if(!StringUtil.equals(oldTypeName, newName) && CSharpNameSuggesterUtil.isIdentifier(copyTypeName))
		{
			PsiElement typeIdentifier = copy.getNameIdentifier();
			if(typeIdentifier != null)
			{
				typeIdentifier.replace(CSharpFileFactory.createIdentifier(project, newName));
			}

			for(DotNetNamedElement element : copy.getMembers())
			{
				if(element instanceof CSharpConstructorDeclaration)
				{
					PsiElement constructorIdentifier = ((CSharpConstructorDeclaration) element).getNameIdentifier();
					if(constructorIdentifier != null)
					{
						constructorIdentifier.replace(CSharpFileFactory.createIdentifier(project, newName));
					}
				}
			}

			List<CSharpReferenceExpression> referenceToChange = new ArrayList<>();
			copy.accept(new CSharpRecursiveElementVisitor()
			{
				@Override
				@RequiredReadAction
				public void visitReferenceExpression(CSharpReferenceExpression expression)
				{
					super.visitReferenceExpression(expression);

					PsiElement resolveTarget = expression.resolve();
					if(expression.kind() == CSharpReferenceExpression.ResolveToKind.THIS || expression.kind() == CSharpReferenceExpression.ResolveToKind.BASE)
					{
						return;
					}

					if(target.isEquivalentTo(resolveTarget) || resolveTarget instanceof CSharpConstructorDeclaration && target.isEquivalentTo(resolveTarget.getParent()))
					{
						referenceToChange.add(expression);
					}
				}
			});

			for(CSharpReferenceExpression expression : referenceToChange)
			{
				expression.handleElementRename(copyTypeName);
			}
		}

		PsiFile file = WriteCommandAction.runWriteCommandAction(project, (Computable<PsiFile>) () ->
		{
			PsiFile psiFile = (PsiFile) targetDirectory.add(copyFile);

			Module module = CreateFileFromTemplateAction.ModuleResolver.EP_NAME.composite().resolveModule(targetDirectory, CSharpFileType.INSTANCE);
			if(module != null)
			{
				ModuleRootManager manager = ModuleRootManager.getInstance(module);
				ModifiableRootModel modifiableModel = manager.getModifiableModel();
				modifiableModel.addContentEntry(psiFile.getVirtualFile());
				modifiableModel.commit();
			}
			return psiFile;
		});

		CopyHandler.updateSelectionInActiveProjectView(file, project, doClone);
		if(openInEditor)
		{
			if(!(file instanceof PsiBinaryFile))
			{
				EditorHelper.openInEditor(file);
				ToolWindowManager.getInstance(project).activateEditorComponent();
			}
		}
	}

	@Nullable
	@RequiredReadAction
	private static PsiDirectory tryNotNullizeDirectory(@Nonnull Project project, @Nullable PsiDirectory defaultTargetDirectory)
	{
		if(defaultTargetDirectory == null)
		{
			VirtualFile root = ArrayUtil.getFirstElement(ProjectRootManager.getInstance(project).getContentRoots());
			if(root == null)
			{
				root = project.getBaseDir();
			}
			if(root == null)
			{
				root = VfsUtil.getUserHomeDir();
			}
			defaultTargetDirectory = root != null ? PsiManager.getInstance(project).findDirectory(root) : null;

			if(defaultTargetDirectory == null)
			{
				LOG.warn("No directory found for project: " + project.getName() + ", root: " + root);
			}
		}
		return defaultTargetDirectory;
	}
}
