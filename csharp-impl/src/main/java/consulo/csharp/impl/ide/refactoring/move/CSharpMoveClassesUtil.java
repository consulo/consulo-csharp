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
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.dotnet.psi.resolve.DotNetPsiSearcher;
import consulo.language.editor.refactoring.RefactoringBundle;
import consulo.language.editor.refactoring.RefactoringSettings;
import consulo.language.editor.refactoring.copy.CopyFilesOrDirectoriesHandler;
import consulo.language.editor.refactoring.move.MoveCallback;
import consulo.language.editor.refactoring.move.MoveHandler;
import consulo.language.editor.refactoring.move.fileOrDirectory.MoveFilesOrDirectoriesDialog;
import consulo.language.editor.refactoring.move.fileOrDirectory.MoveFilesOrDirectoriesUtil;
import consulo.language.editor.refactoring.util.CommonRefactoringUtil;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiFileSystemItem;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.IncorrectOperationException;
import consulo.logging.Logger;
import consulo.project.Project;
import consulo.ui.ex.awt.DialogWrapper;
import consulo.undoRedo.CommandProcessor;
import consulo.util.collection.ContainerUtil;
import consulo.util.lang.Couple;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2019-07-20
 */
public class CSharpMoveClassesUtil
{
	private static final Logger LOG = Logger.getInstance(CSharpMoveClassesUtil.class);

	public static void doMove(final Project project,
							  final PsiElement[] elements,
							  final PsiElement[] targetElement,
							  final MoveCallback moveCallback)
	{
		doMove(project, elements, targetElement, moveCallback, oldElements -> {
			PsiElement[] newElements = new PsiElement[oldElements.length];

			for(int i = 0; i < oldElements.length; i++)
			{
				PsiElement oldElement = oldElements[i];
				if(oldElement instanceof CSharpTypeDeclaration)
				{
					newElements[i] = oldElement.getContainingFile();
				}
				else
				{
					newElements[i] = oldElement;
				}
			}

			return newElements;
		});
	}

	public static void doMove(final Project project,
							  final PsiElement[] elements,
							  final PsiElement[] targetElement,
							  final MoveCallback moveCallback,
							  final Function<PsiElement[], PsiElement[]> adjustElements)
	{
		final PsiDirectory targetDirectory = MoveFilesOrDirectoriesUtil.resolveToDirectory(project, targetElement[0]);
		if(targetElement[0] != null && targetDirectory == null)
		{
			return;
		}

		final PsiElement[] newElements = adjustElements != null ? adjustElements.apply(elements) : elements;

		final PsiDirectory initialTargetDirectory = MoveFilesOrDirectoriesUtil.getInitialTargetDirectory(targetDirectory, elements);

		final MoveFilesOrDirectoriesDialog.Callback doRun = new MoveFilesOrDirectoriesDialog.Callback()
		{
			@Override
			public void run(final MoveFilesOrDirectoriesDialog moveDialog)
			{
				CommandProcessor.getInstance().executeCommand(project, () -> {
					final PsiDirectory targetDirectory1 = moveDialog != null ? moveDialog.getTargetDirectory() : initialTargetDirectory;
					if(targetDirectory1 == null)
					{
						LOG.error("ItemPresentationImpl is null! The target directory, it is null!");
						return;
					}

					Collection<PsiElement> toCheck = ContainerUtil.newArrayList((PsiElement) targetDirectory1);
					for(PsiElement e : newElements)
					{
						toCheck.add(e instanceof PsiFileSystemItem && e.getParent() != null ? e.getParent() : e);
					}
					if(!CommonRefactoringUtil.checkReadOnlyStatus(project, toCheck, false))
					{
						return;
					}

					targetElement[0] = targetDirectory1;

					try
					{
						final int[] choice = elements.length > 1 || elements[0] instanceof PsiDirectory ? new int[]{-1} : null;
						final List<PsiElement> els = new ArrayList<>();
						for(final PsiElement psiElement : newElements)
						{
							if(psiElement instanceof PsiFile)
							{
								final PsiFile file = (PsiFile) psiElement;

								if(CopyFilesOrDirectoriesHandler.checkFileExist(targetDirectory1, choice, file, file.getName(), "Move"))
								{
									continue;
								}
							}
							MoveFilesOrDirectoriesUtil.checkMove(psiElement, targetDirectory1);
							els.add(psiElement);
						}
						final Runnable callback = () -> {
							if(moveDialog != null)
							{
								moveDialog.close(DialogWrapper.CANCEL_EXIT_CODE);
							}
						};
						if(els.isEmpty())
						{
							callback.run();
							return;
						}
						new CSharpClassesMoveProcessor(project, els.toArray(new PsiElement[els.size()]), targetDirectory1, RefactoringSettings.getInstance().MOVE_SEARCH_FOR_REFERENCES_FOR_FILE,
								false, false, moveCallback, callback).run();
					}
					catch(IncorrectOperationException e)
					{
						CommonRefactoringUtil.showErrorMessage(RefactoringBundle.message("error.title"), e.getMessage(), "refactoring.moveFile", project);
					}
				}, MoveHandler.REFACTORING_NAME, null);
			}
		};

		final MoveFilesOrDirectoriesDialog moveDialog = new MoveFilesOrDirectoriesDialog(project, doRun);
		moveDialog.setData(newElements, initialTargetDirectory, "refactoring.moveFile");
		moveDialog.show();
	}


	/**
	 * Return couple of elements.
	 *
	 * If first element is type - second will be same element
	 *
	 * if first element is namespace declaration - second will namespace as element (global namespace object not c# file declaration)
	 */
	@Nonnull
	public static Set<Couple<DotNetNamedElement>> findTypesAndNamespaces(@Nonnull PsiElement element)
	{
		Set<Couple<DotNetNamedElement>> result = new LinkedHashSet<>();
		element.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
			{
				super.visitTypeDeclaration(declaration);

				result.add(Couple.of(declaration, declaration));
			}

			@Override
			@RequiredReadAction
			public void visitNamespaceDeclaration(CSharpNamespaceDeclaration declaration)
			{
				super.visitNamespaceDeclaration(declaration);

				DotNetPsiSearcher searcher = DotNetPsiSearcher.getInstance(declaration.getProject());
				DotNetNamespaceAsElement namespace = searcher.findNamespace(declaration.getPresentableQName(), GlobalSearchScope.projectScope(declaration.getProject()));

				result.add(Couple.<DotNetNamedElement>of(declaration, namespace));
			}
		});
		return result;
	}
}
