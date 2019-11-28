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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.codeInsight.CodeInsightUtilBase;
import com.intellij.codeInsight.editorActions.moveUpDown.LineMover;
import com.intellij.codeInsight.editorActions.moveUpDown.LineRange;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.impl.source.tree.Factory;
import com.intellij.psi.impl.source.tree.TreeElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpEnumConstantDeclaration;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;

/**
 * initial version from java com.intellij.openapi.editor.actions.moveUpDown.DeclarationMover
 */
public class CSharpDeclarationMover extends LineMover
{
	private static final Logger LOG = Logger.getInstance(CSharpDeclarationMover.class);
	private CSharpEnumConstantDeclaration myEnumToInsertSemicolonAfter;

	@RequiredReadAction
	@Override
	public void beforeMove(@Nonnull final Editor editor, @Nonnull final MoveInfo info, final boolean down)
	{
		super.beforeMove(editor, info, down);

		if(myEnumToInsertSemicolonAfter != null)
		{
			TreeElement semicolon = Factory.createSingleLeafElement(CSharpTokens.SEMICOLON, ";", 0, 1, null, myEnumToInsertSemicolonAfter.getManager());

			try
			{
				PsiElement inserted = myEnumToInsertSemicolonAfter.getParent().addAfter(semicolon.getPsi(), myEnumToInsertSemicolonAfter);
				inserted = CodeInsightUtilBase.forcePsiPostprocessAndRestoreElement(inserted);
				final LogicalPosition position = editor.offsetToLogicalPosition(inserted.getTextRange().getEndOffset());

				info.toMove2 = new LineRange(position.line + 1, position.line + 1);
			}
			catch(IncorrectOperationException e)
			{
				LOG.error(e);
			}
			finally
			{
				myEnumToInsertSemicolonAfter = null;
			}
		}
	}

	@Override
	@RequiredReadAction
	public boolean checkAvailable(@Nonnull final Editor editor, @Nonnull final PsiFile file, @Nonnull final MoveInfo info, final boolean down)
	{
		if(!(file instanceof CSharpFile))
		{
			return false;
		}

		boolean available = super.checkAvailable(editor, file, info, down);
		if(!available)
		{
			return false;
		}

		LineRange oldRange = info.toMove;
		final Pair<PsiElement, PsiElement> psiRange = getElementRange(editor, file, oldRange);
		if(psiRange == null)
		{
			return false;
		}

		final DotNetNamedElement firstMember = PsiTreeUtil.getParentOfType(psiRange.getFirst(), DotNetNamedElement.class, false);
		final DotNetNamedElement lastMember = PsiTreeUtil.getParentOfType(psiRange.getSecond(), DotNetNamedElement.class, false);
		if(firstMember == null || lastMember == null)
		{
			return false;
		}

		LineRange range;
		if(firstMember == lastMember)
		{
			range = memberRange(firstMember, editor, oldRange);
			if(range == null)
			{
				return false;
			}
			range.firstElement = range.lastElement = firstMember;
		}
		else
		{
			final PsiElement parent = PsiTreeUtil.findCommonParent(firstMember, lastMember);
			if(parent == null)
			{
				return false;
			}

			final Pair<PsiElement, PsiElement> combinedRange = getElementRange(parent, firstMember, lastMember);
			if(combinedRange == null)
			{
				return false;
			}
			final LineRange lineRange1 = memberRange(combinedRange.getFirst(), editor, oldRange);
			if(lineRange1 == null)
			{
				return false;
			}
			final LineRange lineRange2 = memberRange(combinedRange.getSecond(), editor, oldRange);
			if(lineRange2 == null)
			{
				return false;
			}
			range = new LineRange(lineRange1.startLine, lineRange2.endLine);
			range.firstElement = combinedRange.getFirst();
			range.lastElement = combinedRange.getSecond();
		}
		Document document = editor.getDocument();

		PsiElement sibling = down ? range.lastElement.getNextSibling() : range.firstElement.getPrevSibling();
		if(sibling == null)
		{
			return false;
		}
		sibling = firstNonWhiteElement(sibling, down);
		final boolean areWeMovingClass = range.firstElement instanceof CSharpTypeDeclaration;
		info.toMove = range;
		try
		{
			LineRange intraClassRange = moveInsideOutsideClassPosition(editor, sibling, down, areWeMovingClass);
			if(intraClassRange == null)
			{
				info.toMove2 = new LineRange(sibling, sibling, document);
				if(down && sibling.getNextSibling() == null)
				{
					return false;
				}
			}
			else
			{
				info.toMove2 = intraClassRange;
			}
		}
		catch(IllegalMoveException e)
		{
			info.toMove2 = null;
		}
		return true;
	}

	@RequiredReadAction
	private static LineRange memberRange(@Nonnull PsiElement member, Editor editor, LineRange lineRange)
	{
		final TextRange textRange = member.getTextRange();
		if(editor.getDocument().getTextLength() < textRange.getEndOffset())
		{
			return null;
		}
		final int startLine = editor.offsetToLogicalPosition(textRange.getStartOffset()).line;
		final int endLine = editor.offsetToLogicalPosition(textRange.getEndOffset()).line + 1;
		if(!isInsideDeclaration(member, startLine, endLine, lineRange, editor))
		{
			return null;
		}

		return new LineRange(startLine, endLine);
	}

	@RequiredReadAction
	private static boolean isInsideDeclaration(@Nonnull final PsiElement member, final int startLine, final int endLine, final LineRange lineRange, final Editor editor)
	{
		// if we positioned on member start or end we'll be able to move it
		if(startLine == lineRange.startLine || startLine == lineRange.endLine || endLine == lineRange.startLine || endLine == lineRange.endLine)
		{
			return true;
		}
		List<PsiElement> memberSuspects = new ArrayList<PsiElement>();
		DotNetModifierList modifierList = member instanceof DotNetModifierListOwner ? ((DotNetModifierListOwner) member).getModifierList() : null;
		if(modifierList != null)
		{
			memberSuspects.add(modifierList);
		}
		if(member instanceof CSharpTypeDeclaration)
		{
			final CSharpTypeDeclaration aClass = (CSharpTypeDeclaration) member;

			PsiElement nameIdentifier = aClass.getNameIdentifier();
			if(nameIdentifier != null)
			{
				memberSuspects.add(nameIdentifier);
			}
		}
		if(member instanceof DotNetLikeMethodDeclaration)
		{
			final DotNetLikeMethodDeclaration method = (DotNetLikeMethodDeclaration) member;
			if(member instanceof PsiNameIdentifierOwner)
			{
				PsiElement nameIdentifier = ((PsiNameIdentifierOwner) method).getNameIdentifier();
				if(nameIdentifier != null)
				{
					memberSuspects.add(nameIdentifier);
				}
			}
			DotNetType returnTypeElement = method.getReturnType();
			if(returnTypeElement != null)
			{
				memberSuspects.add(returnTypeElement);
			}
		}
		if(member instanceof DotNetVariable)
		{
			final DotNetVariable field = (DotNetVariable) member;
			PsiElement nameIdentifier = field.getNameIdentifier();
			memberSuspects.add(nameIdentifier);
			DotNetType typeElement = field.getType();
			if(typeElement != null)
			{
				memberSuspects.add(typeElement);
			}
		}
		TextRange lineTextRange = new TextRange(editor.getDocument().getLineStartOffset(lineRange.startLine), editor.getDocument().getLineEndOffset(lineRange.endLine));
		for(PsiElement suspect : memberSuspects)
		{
			TextRange textRange = suspect.getTextRange();
			if(textRange != null && lineTextRange.intersects(textRange))
			{
				return true;
			}
		}
		return false;
	}

	private static class IllegalMoveException extends Exception
	{
	}

	// null means we are not crossing class border
	// throws IllegalMoveException when corresponding movement has no sense
	@Nullable
	@RequiredReadAction
	private LineRange moveInsideOutsideClassPosition(Editor editor, PsiElement sibling, final boolean isDown, boolean areWeMovingClass) throws IllegalMoveException
	{
		if(sibling == null)
		{
			throw new IllegalMoveException();
		}
		if(sibling != null && PsiUtilCore.getElementType(sibling) == (isDown ? CSharpTokens.RBRACE : CSharpTokens.LBRACE) && sibling.getParent() instanceof CSharpTypeDeclaration)
		{
			// moving outside class
			final CSharpTypeDeclaration aClass = (CSharpTypeDeclaration) sibling.getParent();
			final PsiElement parent = aClass.getParent();
			if(!areWeMovingClass && !(parent instanceof CSharpTypeDeclaration))
			{
				throw new IllegalMoveException();
			}

			PsiElement start = isDown ? sibling : aClass.getModifierList();
			return new LineRange(start, sibling, editor.getDocument());
			//return isDown ? nextLineOffset(editor, aClass.getTextRange().getEndOffset()) : aClass.getTextRange().getStartOffset();
		}
		// trying to move up inside enum constant list, move outside of enum class instead
		if(!isDown && sibling.getParent() instanceof CSharpTypeDeclaration && (sibling != null && PsiUtilCore.getElementType(sibling) == CSharpTokens.SEMICOLON || sibling instanceof
				PsiErrorElement) && firstNonWhiteElement(sibling.getPrevSibling(), false) instanceof CSharpEnumConstantDeclaration)
		{
			CSharpTypeDeclaration aClass = (CSharpTypeDeclaration) sibling.getParent();
			Document document = editor.getDocument();
			int startLine = document.getLineNumber(aClass.getTextRange().getStartOffset());
			int endLine = document.getLineNumber(sibling.getTextRange().getEndOffset()) + 1;
			return new LineRange(startLine, endLine);
		}
		if(sibling instanceof CSharpTypeDeclaration)
		{
			// moving inside class
			CSharpTypeDeclaration aClass = (CSharpTypeDeclaration) sibling;

			if(isDown)
			{
				PsiElement child = aClass.getFirstChild();
				if(child == null)
				{
					throw new IllegalMoveException();
				}
				return new LineRange(child, aClass.isEnum() ? afterEnumConstantsPosition(aClass) : aClass.getLeftBrace(), editor.getDocument());
			}
			else
			{
				PsiElement rBrace = aClass.getRightBrace();
				if(rBrace == null)
				{
					throw new IllegalMoveException();
				}
				return new LineRange(rBrace, rBrace, editor.getDocument());
			}
		}
		return null;
	}

	private PsiElement afterEnumConstantsPosition(final CSharpTypeDeclaration aClass)
	{
		DotNetNamedElement[] fields = aClass.getMembers();
		for(int i = fields.length - 1; i >= 0; i--)
		{
			DotNetNamedElement field = fields[i];
			if(field instanceof CSharpEnumConstantDeclaration)
			{
				PsiElement anchor = firstNonWhiteElement(field.getNextSibling(), true);
				if(!(anchor != null && (PsiUtilCore.getElementType(anchor) == CSharpTokens.SEMICOLON))) {
				anchor = field;
				myEnumToInsertSemicolonAfter = (CSharpEnumConstantDeclaration) field;
			}
				return anchor;
			}
		}
		// no enum constants at all ?
		return aClass.getLeftBrace();
	}
}
