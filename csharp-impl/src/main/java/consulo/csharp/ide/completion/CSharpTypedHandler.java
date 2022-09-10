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

package consulo.csharp.ide.completion;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.codeEditor.EditorEx;
import consulo.codeEditor.HighlighterIterator;
import consulo.codeEditor.ScrollType;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.csharp.lang.impl.psi.UsefulPsiTreeUtil;
import consulo.csharp.lang.impl.psi.source.CSharpMethodBodyImpl;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.document.Document;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetPointerTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefUtil;
import consulo.language.ast.TokenType;
import consulo.language.codeStyle.CodeStyleManager;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.action.TypedHandlerDelegate;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.DumbService;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.Pair;
import consulo.virtualFileSystem.fileType.FileType;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

/**
 * @author VISTALL
 * @since 06.01.14
 */
@ExtensionImpl(id = "csharp")
public class CSharpTypedHandler extends TypedHandlerDelegate
{
	@Override
	@RequiredUIAccess
	public Result beforeCharTyped(char c, Project project, Editor editor, PsiFile file, FileType fileType)
	{
		if(!(file instanceof CSharpFile))
		{
			return Result.CONTINUE;
		}

		if(c == '.')
		{
			if(handleDotAtPointerType(editor, file))
			{
				return Result.STOP;
			}

			autoPopupMemberLookup(project, editor);
		}

		if(c == '#')
		{
			autoPopupMemberLookup(project, editor);
		}

		if(c == ';')
		{
			if(handleSemicolon(editor))
			{
				return Result.STOP;
			}
		}

		if(c == '{')
		{
			int offset = editor.getCaretModel().getOffset();
			if(offset == 0)
			{
				return Result.CONTINUE;
			}

			HighlighterIterator iterator = ((EditorEx) editor).getHighlighter().createIterator(offset - 1);
			while(!iterator.atEnd() && iterator.getTokenType() == TokenType.WHITE_SPACE)
			{
				iterator.retreat();
			}
			if(iterator.atEnd() || iterator.getTokenType() == CSharpTokens.RBRACKET || iterator.getTokenType() == CSharpTokens.EQ)
			{
				return Result.CONTINUE;
			}
			Document doc = editor.getDocument();
			PsiDocumentManager.getInstance(project).commitDocument(doc);
			final PsiElement leaf = file.findElementAt(offset);

			if(PsiTreeUtil.getParentOfType(leaf, CSharpMethodBodyImpl.class, false, DotNetModifierListOwner.class) != null)
			{
				consulo.ide.impl.idea.openapi.editor.EditorModificationUtil.insertStringAtCaret(editor, "{");
				consulo.ide.impl.idea.codeInsight.editorActions.TypedHandler.indentOpenedBrace(project, editor);
				return Result.STOP; // use case: manually wrapping part of method's code in 'if', 'while', etc
			}
		}

		return Result.CONTINUE;
	}

	@Override
	public Result checkAutoPopup(char charTyped, Project project, Editor editor, PsiFile file)
	{
		if(charTyped == '@')
		{
			AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
			return Result.STOP;
		}
		return Result.CONTINUE;
	}

	@Override
	@RequiredUIAccess
	public Result charTyped(char c, Project project, Editor editor, @Nonnull PsiFile file)
	{
		if(c == '/')
		{
			if(DumbService.isDumb(file.getProject()))
			{
				return Result.CONTINUE;
			}

			int offset = editor.getCaretModel().getOffset();
			PsiElement elementAt = file.findElementAt(offset - 1);

			if(elementAt == null)
			{
				return Result.CONTINUE;
			}

			PsiElement prevSibling = elementAt.getPrevSibling();
			if(prevSibling != null && prevSibling.getNode().getElementType() == CSharpTokens.LINE_COMMENT)
			{
				PsiElement prevSiblingSkipWhiteSpaces = UsefulPsiTreeUtil.getPrevSiblingSkipWhiteSpaces(prevSibling, true);
				// we skip if prev token is doc, or whe have already filed comment
				if(prevSiblingSkipWhiteSpaces instanceof CSharpDocRoot || prevSibling.getTextLength() != 2)
				{
					return Result.CONTINUE;
				}
				PsiElement nextSibling = elementAt.getNextSibling();
				if(nextSibling instanceof DotNetQualifiedElement)
				{
					Document document = editor.getDocument();
					int lineOfComment = document.getLineNumber(prevSibling.getTextOffset());
					int lineOfMember = document.getLineNumber(nextSibling.getTextOffset());
					// if we have one than one between elements - skip it
					if((lineOfMember - lineOfComment) > 1)
					{
						return Result.CONTINUE;
					}
					Pair<CharSequence, Integer> pair = buildDocComment((DotNetQualifiedElement) nextSibling, editor, offset);

					document.insertString(offset, pair.getFirst());
					editor.getCaretModel().moveToOffset(pair.getSecond());

					PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());

					PsiElement pos = file.findElementAt(pair.getSecond());
					assert pos != null;
					CSharpDocRoot docRoot = PsiTreeUtil.getParentOfType(pos, CSharpDocRoot.class);
					assert docRoot != null;
					CodeStyleManager.getInstance(project).reformat(docRoot);
				}
			}
		}
		return Result.CONTINUE;
	}

	@Nonnull
	@RequiredUIAccess
	private static Pair<CharSequence, Integer> buildDocComment(DotNetQualifiedElement qualifiedElement, Editor editor, int offset)
	{
		String lineIndent = consulo.ide.impl.idea.codeStyle.CodeStyleFacade.getInstance(qualifiedElement.getProject()).getLineIndent(editor.getDocument(), offset);

		int diffForCaret;
		StringBuilder builder = new StringBuilder(" <summary>\n");
		builder.append(lineIndent).append("/// ");
		diffForCaret = offset + builder.length();
		builder.append('\n');
		builder.append(lineIndent).append("/// </summary>\n");

		if(qualifiedElement instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericParameter[] parameters = ((DotNetGenericParameterListOwner) qualifiedElement).getGenericParameters();

			for(DotNetGenericParameter parameter : parameters)
			{
				String name = parameter.getName();
				builder.append(lineIndent).append("///").append(" <typeparam name=\"");
				if(name != null)
				{
					builder.append(name);
				}
				builder.append("\"></typeparam>\n");
			}
		}

		if(qualifiedElement instanceof DotNetParameterListOwner)
		{
			DotNetParameter[] parameters = ((DotNetParameterListOwner) qualifiedElement).getParameters();

			for(DotNetParameter parameter : parameters)
			{
				String name = parameter.getName();
				builder.append(lineIndent).append("///").append(" <param name=\"");
				if(name != null)
				{
					builder.append(name);
				}
				builder.append("\"></param>\n");
			}
		}

		if(qualifiedElement instanceof DotNetMethodDeclaration)
		{
			DotNetTypeRef returnTypeRef = ((DotNetMethodDeclaration) qualifiedElement).getReturnTypeRef();
			if(!DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, DotNetTypes.System.Void))
			{
				builder.append(lineIndent).append("/// <returns></returns>");
			}
		}

		// if last char is new line, remove it
		if(builder.charAt(builder.length() - 1) == '\n')
		{
			builder.deleteCharAt(builder.length() - 1);
		}
		return Pair.<CharSequence, Integer>create(builder, diffForCaret);
	}

	@RequiredUIAccess
	private static boolean handleDotAtPointerType(Editor editor, PsiFile file)
	{
		if(DumbService.isDumb(file.getProject()))
		{
			return false;
		}
		int offset = editor.getCaretModel().getOffset();

		PsiElement lastElement = file.findElementAt(offset - 1);
		if(lastElement == null)
		{
			return false;
		}

		DotNetExpression expression = null;
		if(lastElement.getParent() instanceof DotNetExpression)
		{
			expression = (DotNetExpression) lastElement.getParent();
		}

		if(expression == null)
		{
			return false;
		}

		DotNetTypeRef typeRef = expression.toTypeRef(true);
		if(typeRef instanceof DotNetPointerTypeRef)
		{
			editor.getDocument().insertString(offset, "->");
			editor.getCaretModel().moveToOffset(offset + 2);
			editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
			return true;
		}
		return false;
	}

	private static boolean handleSemicolon(Editor editor)
	{
		int offset = editor.getCaretModel().getOffset();
		if(offset == editor.getDocument().getTextLength())
		{
			return false;
		}

		char charAt = editor.getDocument().getCharsSequence().charAt(offset);
		if(charAt != ';')
		{
			return false;
		}

		editor.getCaretModel().moveToOffset(offset + 1);
		editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
		return true;
	}

	@RequiredUIAccess
	private static void autoPopupMemberLookup(Project project, final Editor editor)
	{
		AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, new Predicate<PsiFile>()
		{
			@Override
			@RequiredReadAction
			public boolean test(final PsiFile file)
			{
				int offset = editor.getCaretModel().getOffset();

				PsiElement lastElement = file.findElementAt(offset - 1);
				if(lastElement == null)
				{
					return false;
				}

				final PsiElement prevSibling = PsiTreeUtil.prevVisibleLeaf(lastElement);
				if(prevSibling == null || ".".equals(prevSibling.getText()))
				{
					return false;
				}
				PsiElement parent = prevSibling;
				do
				{
					parent = parent.getParent();
				}
				while(parent instanceof CSharpReferenceExpression);

				return true;
			}
		});
	}
}
