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

package org.mustbe.consulo.csharp.ide.completion;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocRoot;
import org.mustbe.consulo.csharp.lang.psi.CSharpFile;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.UsefulPsiTreeUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeStyle.CodeStyleFacade;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 06.01.14
 */
public class CSharpTypedHandler extends TypedHandlerDelegate
{
	@Override
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
		if(c == ';')
		{
			if(handleSemicolon(editor))
			{
				return Result.STOP;
			}
		}
		return Result.CONTINUE;
	}

	@Override
	@RequiredDispatchThread
	public Result charTyped(char c, Project project, Editor editor, @NotNull PsiFile file)
	{
		if(c == '#')
		{
			if(file instanceof CSharpFile)
			{
				AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
			}
		}
		else if(c == '/')
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
				if(prevSiblingSkipWhiteSpaces instanceof CSharpDocRoot)
				{
					return Result.CONTINUE;
				}
				PsiElement nextSibling = elementAt.getNextSibling();
				if(nextSibling instanceof DotNetQualifiedElement)
				{
					Pair<CharSequence, Integer> pair = buildDocComment((DotNetQualifiedElement) nextSibling, editor, offset);

					editor.getDocument().insertString(offset, pair.getFirst());
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

	@NotNull
	private static Pair<CharSequence, Integer> buildDocComment(DotNetQualifiedElement qualifiedElement, Editor editor, int offset)
	{
		String lineIndent = CodeStyleFacade.getInstance(qualifiedElement.getProject()).getLineIndent(editor.getDocument(), offset);

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
			if(!DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, qualifiedElement, DotNetTypes.System.Void))
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

	private static void autoPopupMemberLookup(Project project, final Editor editor)
	{
		AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, new Condition<PsiFile>()
		{
			@Override
			public boolean value(final PsiFile file)
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
