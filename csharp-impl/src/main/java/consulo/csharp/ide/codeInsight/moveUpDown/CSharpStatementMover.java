/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

package consulo.csharp.ide.codeInsight.moveUpDown;

import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.codeInsight.editorActions.moveUpDown.StatementUpDownMover;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.refactoring.util.CSharpRefactoringUtil;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetVariable;

import javax.annotation.Nonnull;

/**
 * initial version from java plugin com.intellij.openapi.editor.actions.moveUpDown.StatementMover
 */
class CSharpStatementMover extends LineMover
{
	private static final Logger LOG = Logger.getInstance(CSharpStatementMover.class);

	private PsiElement statementToSurroundWithCodeBlock;

	@Override
	public void beforeMove(@Nonnull final Editor editor, @Nonnull final MoveInfo info, final boolean down)
	{
		super.beforeMove(editor, info, down);
		if(statementToSurroundWithCodeBlock != null)
		{
			//surroundWithCodeBlock(info, down);
		}
	}

	/*private void surroundWithCodeBlock(@NotNull final MoveInfo info, final boolean down)
	{
		try
		{
			final Document document = PsiDocumentManager.getInstance(statementToSurroundWithCodeBlock.getProject()).getDocument(statementToSurroundWithCodeBlock.getContainingFile());
			int startOffset = document.getLineStartOffset(info.toMove.startLine);
			int endOffset = getLineStartSafeOffset(document, info.toMove.endLine);
			if(document.getText().charAt(endOffset - 1) == '\n')
			{
				endOffset--;
			}
			final RangeMarker lineRangeMarker = document.createRangeMarker(startOffset, endOffset);

			final PsiElementFactory factory = CSharpFileFactory.getInstance(statementToSurroundWithCodeBlock.getProject()).getElementFactory();
			PsiCodeBlock codeBlock = factory.createCodeBlock();
			codeBlock.add(statementToSurroundWithCodeBlock);
			final PsiBlockStatement blockStatement = (PsiBlockStatement) factory.createStatementFromText("{}", statementToSurroundWithCodeBlock);
			blockStatement.getCodeBlock().replace(codeBlock);
			PsiBlockStatement newStatement = (PsiBlockStatement) statementToSurroundWithCodeBlock.replace(blockStatement);
			newStatement = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(newStatement);
			info.toMove = new LineRange(document.getLineNumber(lineRangeMarker.getStartOffset()), document.getLineNumber(lineRangeMarker.getEndOffset()) + 1);
			PsiCodeBlock newCodeBlock = newStatement.getCodeBlock();
			if(down)
			{
				PsiElement blockChild = firstNonWhiteElement(newCodeBlock.getFirstBodyElement(), true);
				if(blockChild == null)
				{
					blockChild = newCodeBlock.getRBrace();
				}
				info.toMove2 = new LineRange(info.toMove2.startLine, //document.getLineNumber(newCodeBlock.getParent().getTextRange().getStartOffset()),
						document.getLineNumber(blockChild.getTextRange().getStartOffset()));
			}
			else
			{
				int start = document.getLineNumber(newCodeBlock.getRBrace().getTextRange().getStartOffset());
				int end = info.toMove.startLine;
				if(start > end)
				{
					end = start;
				}
				info.toMove2 = new LineRange(start, end);
			}
		}
		catch(IncorrectOperationException e)
		{
			LOG.error(e);
		}
	}  */

	@Override
	@RequiredReadAction
	public boolean checkAvailable(@Nonnull final Editor editor, @Nonnull final PsiFile file, @Nonnull final MoveInfo info, final boolean down)
	{
		//if (!(file instanceof PsiJavaFile)) return false;
		final boolean available = super.checkAvailable(editor, file, info, down);
		if(!available)
		{
			return false;
		}
		LineRange range = info.toMove;

		range = expandLineRangeToCoverPsiElements(range, editor, file);
		if(range == null)
		{
			return false;
		}
		info.toMove = range;
		final int startOffset = editor.logicalPositionToOffset(new LogicalPosition(range.startLine, 0));
		final int endOffset = editor.logicalPositionToOffset(new LogicalPosition(range.endLine, 0));
		final PsiElement[] statements = CSharpRefactoringUtil.findStatementsInRange(file, startOffset, endOffset);
		if(statements.length == 0)
		{
			return false;
		}
		range.firstElement = statements[0];
		range.lastElement = statements[statements.length - 1];

		if(!checkMovingInsideOutside(file, editor, range, info, down))
		{
			info.toMove2 = null;
			return true;
		}
		return true;
	}

	@RequiredReadAction
	private int getDestLineForAnon(PsiFile file, Editor editor, LineRange range, MoveInfo info, boolean down)
	{
		int destLine = down ? range.endLine + 1 : range.startLine - 1;
		if(!(range.firstElement instanceof DotNetStatement))
		{
			return destLine;
		}
		PsiElement sibling = StatementUpDownMover.firstNonWhiteElement(down ? range.firstElement.getNextSibling() : range.firstElement.getPrevSibling(), down);
		PsiElement toMove = sibling;
		if(!(sibling instanceof DotNetStatement))
		{
			return destLine;
		}

		if(sibling instanceof DotNetVariable)
		{
			sibling = ((DotNetVariable) sibling).getInitializer();
		}
		if(sibling instanceof CSharpExpressionStatementImpl)
		{
			sibling = ((CSharpExpressionStatementImpl) sibling).getExpression();
		}

		if(!(sibling instanceof CSharpTypeDeclaration))
		{
			return destLine;
		}
		destLine = editor.getDocument().getLineNumber(down ? toMove.getTextRange().getEndOffset() : toMove.getTextRange().getStartOffset());

		return destLine;
	}

	@RequiredReadAction
	private boolean calcInsertOffset(@Nonnull PsiFile file, @Nonnull Editor editor, @Nonnull LineRange range, @Nonnull final MoveInfo info, final boolean down)
	{
		int destLine = getDestLineForAnon(file, editor, range, info, down);

		int startLine = down ? range.endLine : range.startLine - 1;
		if(destLine < 0 || startLine < 0)
		{
			return false;
		}
		while(true)
		{
			final int offset = editor.logicalPositionToOffset(new LogicalPosition(destLine, 0));
			PsiElement element = firstNonWhiteElement(offset, file, true);

			while(element != null && !(element instanceof PsiFile))
			{
				if(!element.getTextRange().grown(-1).shiftRight(1).contains(offset))
				{
					PsiElement elementToSurround = null;
					boolean found = false;
					if((element instanceof DotNetStatement || element instanceof PsiComment) && statementCanBePlacedAlong(element))
					{
						found = true;
						if(!(isCodeBlock(element.getParent())))
						{
							elementToSurround = element;
						}
					}
					else if(PsiUtilCore.getElementType(element) == CSharpTokens.RBRACE && isCodeBlock(element.getParent()))
					{
						// before code block closing brace
						found = true;
					}
					if(found)
					{
						statementToSurroundWithCodeBlock = elementToSurround;
						info.toMove = range;
						int endLine = destLine;
						if(startLine > endLine)
						{
							int tmp = endLine;
							endLine = startLine;
							startLine = tmp;
						}

						info.toMove2 = down ? new LineRange(startLine, endLine) : new LineRange(startLine, endLine + 1);
						return true;
					}
				}
				element = element.getParent();
			}
			destLine += down ? 1 : -1;
			if(destLine == 0 || destLine >= editor.getDocument().getLineCount())
			{
				return false;
			}
		}
	}

	private static boolean statementCanBePlacedAlong(final PsiElement element)
	{
		if(isCodeBlock(element.getParent()))
		{
			return true;
		}
		if(element instanceof CSharpBlockStatementImpl)
		{
			return false;
		}
		final PsiElement parent = element.getParent();

		if(parent instanceof CSharpIfStatementImpl && (element == ((CSharpIfStatementImpl) parent).getTrueStatement() || element == ((CSharpIfStatementImpl) parent).getFalseStatement()))
		{
			return true;
		}
		if(parent instanceof CSharpWhileStatementImpl && element == ((CSharpWhileStatementImpl) parent).getChildStatement())
		{
			return true;
		}
		if(parent instanceof CSharpDoWhileStatementImpl && element == ((CSharpDoWhileStatementImpl) parent).getChildStatement())
		{
			return true;
		}
		// know nothing about that
		return false;
	}

	@RequiredReadAction
	private boolean checkMovingInsideOutside(PsiFile file, final Editor editor, LineRange range, @Nonnull final MoveInfo info, final boolean down)
	{
		final int offset = editor.getCaretModel().getOffset();

		PsiElement elementAtOffset = file.getViewProvider().findElementAt(offset, CSharpLanguage.INSTANCE);
		if(elementAtOffset == null)
		{
			return false;
		}

		PsiElement guard = elementAtOffset;

		guard = PsiTreeUtil.getParentOfType(guard, DotNetLikeMethodDeclaration.class, CSharpTypeDeclaration.class, PsiComment.class);


		PsiElement brace = itIsTheClosingCurlyBraceWeAreMoving(file, editor);
		if(brace != null)
		{
			int line = editor.getDocument().getLineNumber(offset);
			final LineRange toMove = new LineRange(line, line + 1);
			toMove.firstElement = toMove.lastElement = brace;
			info.toMove = toMove;
		}

		// cannot move in/outside method/class/initializer/comment
		if(!calcInsertOffset(file, editor, info.toMove, info, down))
		{
			return false;
		}
		int insertOffset = down ? getLineStartSafeOffset(editor.getDocument(), info.toMove2.endLine) : editor.getDocument().getLineStartOffset(info.toMove2.startLine);
		PsiElement elementAtInsertOffset = file.getViewProvider().findElementAt(insertOffset, CSharpLanguage.INSTANCE);
		PsiElement newGuard = elementAtInsertOffset;

		newGuard = PsiTreeUtil.getParentOfType(newGuard, DotNetLikeMethodDeclaration.class, CSharpTypeDeclaration.class, PsiComment.class);

		if(brace != null && PsiTreeUtil.getParentOfType(brace, CSharpBlockStatementImpl.class, false) != PsiTreeUtil.getParentOfType(elementAtInsertOffset, CSharpBlockStatementImpl.class, false))
		{
			info.indentSource = true;
		}
		if(newGuard == guard && isInside(insertOffset, newGuard) == isInside(offset, guard))
		{
			return true;
		}

		// moving in/out nested class is OK
		if(guard instanceof CSharpTypeDeclaration && guard.getParent() instanceof CSharpTypeDeclaration)
		{
			return true;
		}
		if(newGuard instanceof CSharpTypeDeclaration && newGuard.getParent() instanceof CSharpTypeDeclaration)
		{
			return true;
		}

		return false;
	}

	private static boolean isCodeBlock(PsiElement element)
	{
		return element instanceof CSharpBlockStatementImpl && element.getParent() instanceof DotNetNamedElement;
	}

	@RequiredReadAction
	private static boolean isInside(final int offset, final PsiElement guard)
	{
		if(guard == null)
		{
			return false;
		}
		TextRange inside;
		if(guard instanceof DotNetLikeMethodDeclaration)
		{
			inside = ((DotNetLikeMethodDeclaration) guard).getCodeBlock().getElement().getTextRange();
		}
		else
		{
			if(guard instanceof CSharpTypeDeclaration)
			{
				inside = new TextRange(((CSharpTypeDeclaration) guard).getLeftBrace().getTextOffset(), ((CSharpTypeDeclaration) guard).getRightBrace().getTextOffset());
			}
			else
			{
				inside = guard.getTextRange();
			}
		}
		return inside != null && inside.contains(offset);
	}

	@RequiredReadAction
	private static LineRange expandLineRangeToCoverPsiElements(final LineRange range, Editor editor, final PsiFile file)
	{
		Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, range);
		if(psiRange == null)
		{
			return null;
		}
		final PsiElement parent = PsiTreeUtil.findCommonParent(psiRange.getFirst(), psiRange.getSecond());
		Pair<PsiElement, PsiElement> elementRange = getElementRange(parent, psiRange.getFirst(), psiRange.getSecond());
		if(elementRange == null)
		{
			return null;
		}
		int endOffset = elementRange.getSecond().getTextRange().getEndOffset();
		Document document = editor.getDocument();
		if(endOffset > document.getTextLength())
		{
			LOG.assertTrue(!PsiDocumentManager.getInstance(file.getProject()).isUncommited(document));
			LOG.assertTrue(PsiDocumentManagerImpl.checkConsistency(file, document));
		}
		int endLine;
		if(endOffset == document.getTextLength())
		{
			endLine = document.getLineCount();
		}
		else
		{
			endLine = editor.offsetToLogicalPosition(endOffset).line + 1;
			endLine = Math.min(endLine, document.getLineCount());
		}
		int startLine = Math.min(range.startLine, editor.offsetToLogicalPosition(elementRange.getFirst().getTextOffset()).line);
		endLine = Math.max(endLine, range.endLine);
		return new LineRange(startLine, endLine);
	}

	@RequiredReadAction
	private static PsiElement itIsTheClosingCurlyBraceWeAreMoving(final PsiFile file, final Editor editor)
	{
		LineRange range = getLineRangeFromSelection(editor);
		if(range.endLine - range.startLine != 1)
		{
			return null;
		}
		int offset = editor.getCaretModel().getOffset();
		Document document = editor.getDocument();
		int line = document.getLineNumber(offset);
		int lineStartOffset = document.getLineStartOffset(line);
		String lineText = document.getText().substring(lineStartOffset, document.getLineEndOffset(line));
		if(!lineText.trim().equals("}"))
		{
			return null;
		}

		return file.findElementAt(lineStartOffset + lineText.indexOf('}'));
	}
}

