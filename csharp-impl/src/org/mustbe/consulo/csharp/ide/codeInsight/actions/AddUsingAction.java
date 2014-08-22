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

import java.util.Collection;
import java.util.Set;

import javax.swing.Icon;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.codeInsight.CSharpCodeInsightSettings;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingListImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeByQNameIndex;
import org.mustbe.consulo.dotnet.DotNetBundle;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryImpl;
import org.mustbe.consulo.dotnet.module.roots.DotNetLibraryOrderEntryTypeProvider;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
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
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.impl.ModuleRootLayerImpl;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiParserFacade;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
@Logger
public class AddUsingAction implements QuestionAction
{
	private final Editor myEditor;
	private final Project myProject;
	private final CSharpReferenceExpressionImpl myRef;
	private final Set<Couple<String>> myElements;

	public AddUsingAction(Editor editor, CSharpReferenceExpressionImpl ref, Set<Couple<String>> q)
	{
		myEditor = editor;
		myRef = ref;
		myProject = ref.getProject();
		myElements = q;
	}

	private PsiElement getElementForBeforeAdd()
	{
		PsiFile containingFile = myRef.getContainingFile();
		for(PsiElement psiElement : containingFile.getChildren())
		{
			if(psiElement instanceof CSharpUsingListImpl)
			{
				return psiElement;
			}
		}

		return containingFile;
	}

	@Override
	public boolean execute()
	{
		PsiDocumentManager.getInstance(myProject).commitAllDocuments();

		Couple<String> firstCount = myElements.size() == 1 ? ContainerUtil.getFirstItem(myElements) : null;

		if(firstCount != null && firstCount.getFirst() == null)
		{
			execute0(ContainerUtil.getFirstItem(myElements));
		}
		else
		{
			BaseListPopupStep<Couple<String>> step = new BaseListPopupStep<Couple<String>>(DotNetBundle.message("add.using"),
					myElements.toArray(new Couple[myElements.size()]))
			{
				@Override
				public Icon getIconFor(Couple<String> aValue)
				{
					return AllIcons.Nodes.Package;
				}

				@NotNull
				@Override
				public String getTextFor(Couple<String> value)
				{
					String first = value.getFirst();
					if(first == null)
					{
						return value.getSecond();
					}
					else
					{
						return value.getSecond() + " in '" + value.getFirst() + "'";
					}
				}

				@Override
				public PopupStep onChosen(final Couple<String> selectedValue, boolean finalChoice)
				{
					execute0(selectedValue);
					return FINAL_CHOICE;
				}
			};

			JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(myEditor);
		}

		return true;
	}

	private void execute0(final Couple<String> couple)
	{
		PsiDocumentManager.getInstance(myProject).commitAllDocuments();

		new WriteCommandAction<Object>(myRef.getProject(), myRef.getContainingFile())
		{
			@Override
			protected void run(Result<Object> objectResult) throws Throwable
			{
				addUsing(couple.getSecond());

				String first = couple.getFirst();
				if(first != null)
				{
					Module moduleForFile = ModuleUtilCore.findModuleForPsiElement(myRef);
					if(moduleForFile != null)
					{
						ModuleRootManager moduleRootManager = ModuleRootManager.getInstance(moduleForFile);

						val modifiableModel = moduleRootManager.getModifiableModel();

						modifiableModel.addOrderEntry(new DotNetLibraryOrderEntryImpl(DotNetLibraryOrderEntryTypeProvider.getInstance(),
								(ModuleRootLayerImpl) moduleRootManager.getCurrentLayer(), first));

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

	private void addUsing(String qName)
	{
		PsiElement elementForBeforeAdd = getElementForBeforeAdd();

		if(elementForBeforeAdd instanceof CSharpUsingListImpl)
		{
			((CSharpUsingListImpl) elementForBeforeAdd).addUsing(qName);
		}
		else if(elementForBeforeAdd instanceof CSharpFileImpl)
		{
			DotNetQualifiedElement[] members = ((CSharpFileImpl) elementForBeforeAdd).getMembers();

			PsiElement firstChild = members.length > 0 ? members[0] : elementForBeforeAdd.getFirstChild();

			assert firstChild != null;

			CSharpUsingListImpl usingStatement = CSharpFileFactory.createUsingList(myProject, qName);

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
			bindToRef(qName);

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

	private void bindToRef(String qName)
	{
		Collection<DotNetTypeDeclaration> list = TypeByQNameIndex.getInstance().get(qName + "." + myRef.getReferenceName(), myProject,
				myRef.getResolveScope());

		if(!list.isEmpty())
		{
			myRef.bindToElement(list.iterator().next());
		}
	}
}
