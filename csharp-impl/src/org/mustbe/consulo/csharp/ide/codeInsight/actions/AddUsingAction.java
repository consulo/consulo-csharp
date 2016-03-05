/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import java.util.Set;

import javax.swing.Icon;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.codeInsight.CSharpCodeInsightSettings;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import org.mustbe.consulo.dotnet.DotNetBundle;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryImpl;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.codeInsight.actions.OptimizeImportsProcessor;
import com.intellij.codeInsight.hint.QuestionAction;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
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
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;
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

/**
 * @author VISTALL
 * @since 30.12.13.
 */
@Logger
public class AddUsingAction implements QuestionAction
{
	private final Editor myEditor;
	private final Project myProject;
	private final PsiFile myFile;
	private final Set<NamespaceReference> myElements;

	public AddUsingAction(Editor editor, PsiFile file, Set<NamespaceReference> references)
	{
		myEditor = editor;
		myFile = file;
		myProject = file.getProject();
		myElements = references;
	}

	public AddUsingAction(Editor editor, CSharpReferenceExpression ref, Set<NamespaceReference> references)
	{
		myEditor = editor;
		myFile = ref.getContainingFile();
		myProject = ref.getProject();
		myElements = references;
	}

	@NotNull
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
			BaseListPopupStep<NamespaceReference> step = new BaseListPopupStep<NamespaceReference>(DotNetBundle.message("add.using"),
					myElements.toArray(new NamespaceReference[myElements.size()]))
			{
				@Override
				public Icon getIconFor(NamespaceReference aValue)
				{
					return AllIcons.Nodes.Package;
				}

				@NotNull
				@Override
				public String getTextFor(NamespaceReference value)
				{
					return formatMessage(value);
				}

				@Override@RequiredDispatchThread

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

	@NotNull
	public static String formatMessage(@NotNull NamespaceReference couple)
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
	private void addUsing(String qName)
	{
		PsiElement elementForBeforeAdd = getElementForBeforeAdd();

		if(elementForBeforeAdd instanceof CSharpUsingListChild)
		{
			addUsingStatementAfter((CSharpUsingListChild) elementForBeforeAdd, qName);
		}
		else if(elementForBeforeAdd instanceof CSharpFile)
		{
			DotNetQualifiedElement[] members = ((CSharpFile) elementForBeforeAdd).getMembers();

			PsiElement firstChild = members.length > 0 ? members[0] : elementForBeforeAdd.getFirstChild();

			assert firstChild != null;

			CSharpUsingNamespaceStatement usingStatement = CSharpFileFactory.createUsingNamespaceStatement(myProject, qName);

			PsiElement usingStatementNew = elementForBeforeAdd.addBefore(usingStatement, firstChild);

			PsiElement whiteSpaceFromText = PsiParserFacade.SERVICE.getInstance(myProject).createWhiteSpaceFromText("\n\n");

			elementForBeforeAdd.addAfter(whiteSpaceFromText, usingStatementNew);
		}

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

	@RequiredReadAction
	private static void addUsingStatementAfter(@NotNull PsiElement afterElement, @NotNull String qName)
	{
		Project project = afterElement.getProject();

		CSharpUsingNamespaceStatement newStatement = CSharpFileFactory.createUsingNamespaceStatement(project, qName);

		PsiElement parent = afterElement.getParent();

		PsiElement whiteSpaceFromText = PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText("\n");

		parent.addAfter(whiteSpaceFromText, afterElement);

		parent.addAfter(newStatement, afterElement.getNode().getTreeNext().getPsi());
	}
}
