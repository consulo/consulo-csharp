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
package consulo.csharp.impl.ide.completion.util;

import javax.annotation.Nullable;

import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiWhiteSpace;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.csharp.impl.ide.parameterInfo.CSharpParameterInfoHandler;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.language.editor.AutoPopupController;
import consulo.language.editor.completion.lookup.InsertHandler;
import consulo.language.editor.completion.lookup.InsertionContext;
import consulo.document.Document;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiElement;
import consulo.util.lang.StringUtil;

/**
 * @author peter
 */
public class CSharpParenthesesInsertHandler implements InsertHandler<LookupElement>
{
	private DotNetLikeMethodDeclaration myLikeMethodDeclaration;

	public CSharpParenthesesInsertHandler(DotNetLikeMethodDeclaration likeMethodDeclaration)
	{
		myLikeMethodDeclaration = likeMethodDeclaration;
	}

	@RequiredUIAccess
	private static boolean isToken(@Nullable final PsiElement element, final String text)
	{
		return element != null && text.equals(element.getText());
	}

	@RequiredUIAccess
	private boolean placeCaretInsideParentheses()
	{
		DotNetParameterList parameterList = myLikeMethodDeclaration.getParameterList();
		return parameterList != null && parameterList.getParametersCount() > 0;
	}

	@RequiredUIAccess
	@Override
	public void handleInsert(final InsertionContext context, final LookupElement item)
	{
		final Editor editor = context.getEditor();
		final Document document = editor.getDocument();
		context.commitDocument();
		PsiElement element = findNextToken(context);

		final char completionChar = context.getCompletionChar();
		final boolean putCaretInside = completionChar == '(' || placeCaretInsideParentheses();

		if(completionChar == '(')
		{
			context.setAddCompletionChar(false);
		}

		if(isToken(element, "("))
		{
			int lparenthOffset = element.getTextRange().getStartOffset();

			if(completionChar == '(' || completionChar == '\t')
			{
				editor.getCaretModel().moveToOffset(lparenthOffset + 1);
			}
			else
			{
				editor.getCaretModel().moveToOffset(context.getTailOffset());
			}

			context.setTailOffset(lparenthOffset + 1);

			PsiElement list = element.getParent();
			PsiElement last = list.getLastChild();
			if(isToken(last, ")"))
			{
				int rparenthOffset = last.getTextRange().getStartOffset();
				context.setTailOffset(rparenthOffset + 1);
				if(!putCaretInside)
				{
					for(int i = lparenthOffset + 1; i < rparenthOffset; i++)
					{
						if(!Character.isWhitespace(document.getCharsSequence().charAt(i)))
						{
							return;
						}
					}
					editor.getCaretModel().moveToOffset(context.getTailOffset());
				}
				else
				{
					AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, CSharpParameterInfoHandler.item(myLikeMethodDeclaration));

					editor.getCaretModel().moveToOffset(lparenthOffset + 1);
				}
				return;
			}
		}
		else
		{
			document.insertString(context.getTailOffset(), "" + "(" + "");
			editor.getCaretModel().moveToOffset(context.getTailOffset());
		}

		if(context.getCompletionChar() == '(')
		{
			//todo use BraceMatchingUtil.isPairedBracesAllowedBeforeTypeInFileType
			int tail = context.getTailOffset();
			if(tail < document.getTextLength() && StringUtil.isJavaIdentifierPart(document.getCharsSequence().charAt(tail)))
			{
				return;
			}
		}

		document.insertString(context.getTailOffset(), ")");
		if(!putCaretInside)
		{
			editor.getCaretModel().moveToOffset(context.getTailOffset());
		}
		else
		{
			AutoPopupController.getInstance(context.getProject()).autoPopupParameterInfo(editor, CSharpParameterInfoHandler.item(myLikeMethodDeclaration));
		}
	}

	@Nullable
	@RequiredUIAccess
	protected PsiElement findNextToken(final InsertionContext context)
	{
		final PsiFile file = context.getFile();
		PsiElement element = file.findElementAt(context.getTailOffset());
		if(element instanceof PsiWhiteSpace)
		{
			boolean allowParametersOnNextLine = false;
			if(!allowParametersOnNextLine && element.getText().contains("\n"))
			{
				return null;
			}
			element = file.findElementAt(element.getTextRange().getEndOffset());
		}
		return element;
	}

}
