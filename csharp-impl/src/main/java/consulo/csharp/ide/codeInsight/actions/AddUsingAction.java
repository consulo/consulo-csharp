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

package consulo.csharp.ide.codeInsight.actions;

import consulo.annotation.access.RequiredWriteAction;
import consulo.application.WriteAction;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorPopupHelper;
import consulo.codeEditor.LogicalPosition;
import consulo.codeEditor.ScrollType;
import consulo.csharp.ide.codeInsight.CSharpCodeInsightSettings;
import consulo.csharp.lang.impl.psi.source.using.AddUsingUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.document.Document;
import consulo.document.RangeMarker;
import consulo.dotnet.DotNetBundle;
import consulo.dotnet.impl.roots.orderEntry.DotNetLibraryOrderEntryModel;
import consulo.dotnet.impl.roots.orderEntry.DotNetLibraryOrderEntryType;
import consulo.dotnet.libraryAnalyzer.NamespaceReference;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.hint.QuestionAction;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.language.util.ModuleUtilCore;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.popup.BaseListPopupStep;
import consulo.ui.ex.popup.JBPopupFactory;
import consulo.ui.ex.popup.PopupStep;
import consulo.ui.image.Image;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class AddUsingAction implements QuestionAction
{
	private static final Logger LOGGER = Logger.getInstance(AddUsingAction.class);

	@Nullable
	private final Editor myEditor;
	private final Project myProject;
	private final PsiFile myFile;
	private final Set<NamespaceReference> myElements;

	public AddUsingAction(@Nullable Editor editor, PsiFile file, Set<NamespaceReference> references)
	{
		myEditor = editor;
		myFile = file;
		myProject = file.getProject();
		myElements = references;
	}

	public AddUsingAction(@Nullable Editor editor, CSharpReferenceExpression ref, Set<NamespaceReference> references)
	{
		myEditor = editor;
		myFile = ref.getContainingFile();
		myProject = ref.getProject();
		myElements = references;
	}

	@Override
	@RequiredUIAccess
	public boolean execute()
	{
		PsiDocumentManager.getInstance(myProject).commitAllDocuments();

		NamespaceReference firstCount = myElements.size() == 1 ? ContainerUtil.getFirstItem(myElements) : null;

		if(firstCount != null)
		{
			execute0(ContainerUtil.getFirstItem(myElements));
		}
		else
		{
			BaseListPopupStep<NamespaceReference> step = new BaseListPopupStep<NamespaceReference>(DotNetBundle.message("add.using"), myElements.toArray(new NamespaceReference[myElements.size()]))
			{
				@Override
				public Image getIconFor(NamespaceReference aValue)
				{
					return PlatformIconGroup.nodesNamespace();
				}

				@Nonnull
				@Override
				public String getTextFor(NamespaceReference value)
				{
					return formatMessage(value);
				}

				@Override
				@RequiredUIAccess

				public PopupStep onChosen(final NamespaceReference selectedValue, boolean finalChoice)
				{
					execute0(selectedValue);
					return FINAL_CHOICE;
				}
			};

			EditorPopupHelper.getInstance().showPopupInBestPositionFor(myEditor, JBPopupFactory.getInstance().createListPopup(step));
		}

		return true;
	}

	@Nonnull
	public static String formatMessage(@Nonnull NamespaceReference couple)
	{
		String libraryName = couple.getLibraryName();
		String namespace = couple.getNamespace();
		if(libraryName == null)
		{
			return namespace;
		}

		return namespace + " from '" + libraryName + "'";
	}

	@RequiredUIAccess
	private void execute0(final NamespaceReference namespaceReference)
	{
		PsiDocumentManager.getInstance(myProject).commitAllDocuments();

		WriteCommandAction.runWriteCommandAction(myProject, () ->
		{
			addUsing(namespaceReference.getNamespace());

			String libraryName = namespaceReference.getLibraryName();
			if(libraryName != null)
			{
				Module moduleForFile = ModuleUtilCore.findModuleForPsiElement(myFile);
				if(moduleForFile != null)
				{
					ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(moduleForFile);

					final ModifiableRootModel modifiableModel = moduleRootManager.getModifiableModel();

					modifiableModel.addCustomOderEntry(DotNetLibraryOrderEntryType.getInstance(), new DotNetLibraryOrderEntryModel(libraryName));

					WriteAction.run(modifiableModel::commit);
				}
			}
		});
	}

	@RequiredWriteAction
	public void addUsing(String qName)
	{
		AddUsingUtil.addUsingNoCaretMoving(myFile, qName);

		assert myEditor != null;

		int caretOffset = myEditor.getCaretModel().getOffset();
		RangeMarker caretMarker = myEditor.getDocument().createRangeMarker(caretOffset, caretOffset);
		int colByOffset = myEditor.offsetToLogicalPosition(caretOffset).column;
		int col = myEditor.getCaretModel().getLogicalPosition().column;
		int virtualSpace = col == colByOffset ? 0 : col - colByOffset;
		int line = myEditor.getCaretModel().getLogicalPosition().line;
		LogicalPosition pos = new LogicalPosition(line, 0);
		myEditor.getCaretModel().moveToLogicalPosition(pos);

		try
		{
			if(CSharpCodeInsightSettings.getInstance().OPTIMIZE_IMPORTS_ON_THE_FLY)
			{
				Document document = myEditor.getDocument();
				PsiFile psiFile = PsiDocumentManager.getInstance(myProject).getPsiFile(document);
				new consulo.ide.impl.idea.codeInsight.actions.OptimizeImportsProcessor(myProject, psiFile).runWithoutProgress();
			}
		}
		catch(IncorrectOperationException e)
		{
			LOGGER.error(e);
		}

		line = myEditor.getCaretModel().getLogicalPosition().line;
		LogicalPosition pos1 = new LogicalPosition(line, col);
		myEditor.getCaretModel().moveToLogicalPosition(pos1);
		if(caretMarker.isValid())
		{
			LogicalPosition pos2 = myEditor.offsetToLogicalPosition(caretMarker.getStartOffset());
			int newCol = pos2.column + virtualSpace;
			myEditor.getCaretModel().moveToLogicalPosition(new LogicalPosition(pos2.line, newCol));
			myEditor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
		}
	}
}
