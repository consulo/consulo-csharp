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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.swing.Icon;

import javax.annotation.Nullable;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.codeInsight.CSharpCodeInsightSettings;
import consulo.csharp.lang.psi.CSharpCodeFragment;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.dotnet.DotNetBundle;
import consulo.dotnet.libraryAnalyzer.NamespaceReference;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.roots.orderEntry.DotNetLibraryOrderEntryImpl;
import consulo.roots.impl.ModuleRootLayerImpl;

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

	@Nonnull
	@RequiredReadAction
	private PsiElement getElementForBeforeAdd()
	{
		if(myFile instanceof CSharpFile)
		{
			CSharpUsingListChild[] usingStatements = ((CSharpFile) myFile).getUsingStatements();
			if(usingStatements.length > 0)
			{
				return ArrayUtil.getLastElement(usingStatements);
			}
		}
		return myFile;
	}

	@Override
	@RequiredDispatchThread
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
				public Icon getIconFor(NamespaceReference aValue)
				{
					return AllIcons.Nodes.Package;
				}

				@Nonnull
				@Override
				public String getTextFor(NamespaceReference value)
				{
					return formatMessage(value);
				}

				@Override
				@RequiredDispatchThread

				public PopupStep onChosen(final NamespaceReference selectedValue, boolean finalChoice)
				{
					execute0(selectedValue);
					return FINAL_CHOICE;
				}
			};

			JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(myEditor);
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

	@RequiredDispatchThread
	private void execute0(final NamespaceReference namespaceReference)
	{
		PsiDocumentManager.getInstance(myProject).commitAllDocuments();

		new WriteCommandAction<Object>(myProject, myFile)
		{
			@Override
			@RequiredReadAction
			protected void run(Result<Object> objectResult) throws Throwable
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

						modifiableModel.addOrderEntry(new DotNetLibraryOrderEntryImpl((ModuleRootLayerImpl) moduleRootManager.getCurrentLayer(), libraryName));

						new WriteCommandAction<Object>(moduleForFile.getProject())
						{
							@Override
							protected void run(Result<Object> objectResult) throws Throwable
							{
								modifiableModel.commit();
							}
						}.execute();
					}
				}
			}
		}.execute();
	}

	@RequiredReadAction
	public void addUsing(String qName)
	{
		addUsingNoCaretMoving(qName);

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
				new OptimizeImportsProcessor(myProject, psiFile).runWithoutProgress();
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

	@RequiredWriteAction
	public void addUsingNoCaretMoving(String qName)
	{
		PsiElement elementForBeforeAdd = getElementForBeforeAdd();

		CSharpUsingNamespaceStatement newStatement = CSharpFileFactory.createUsingNamespaceStatement(myProject, qName);

		if(myFile instanceof CSharpCodeFragment)
		{
			((CSharpCodeFragment) myFile).addUsingChild(newStatement);
		}
		else if(elementForBeforeAdd instanceof CSharpUsingListChild)
		{
			addUsingStatementAfter(elementForBeforeAdd, newStatement);
		}
		else if(elementForBeforeAdd instanceof CSharpFile)
		{
			DotNetQualifiedElement[] members = ((CSharpFile) elementForBeforeAdd).getMembers();

			PsiElement firstChild = members.length > 0 ? members[0] : elementForBeforeAdd.getFirstChild();

			assert firstChild != null;

			PsiElement usingStatementNew = elementForBeforeAdd.addBefore(newStatement, firstChild);

			PsiElement whiteSpaceFromText = PsiParserFacade.SERVICE.getInstance(myProject).createWhiteSpaceFromText("\n\n");

			elementForBeforeAdd.addAfter(whiteSpaceFromText, usingStatementNew);
		}
	}

	@RequiredReadAction
	private static void addUsingStatementAfter(@Nonnull PsiElement afterElement, @Nonnull CSharpUsingNamespaceStatement newStatement)
	{
		Project project = afterElement.getProject();

		PsiElement parent = afterElement.getParent();

		PsiElement whiteSpaceFromText = PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n");

		parent.addAfter(whiteSpaceFromText, afterElement);

		parent.addAfter(newStatement, afterElement.getNode().getTreeNext().getPsi());
	}
}
