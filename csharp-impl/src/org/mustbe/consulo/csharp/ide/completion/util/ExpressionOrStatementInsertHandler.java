/*
 * Copyright 2000-2009 JetBrains s.r.o.
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.completion.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeStyle.CSharpCodeStyleSettings;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;

/**
 * @author peter
 * @see com.intellij.codeInsight.completion.util.ParenthesesInsertHandler
 */
public abstract class ExpressionOrStatementInsertHandler<T extends LookupElement> implements InsertHandler<T>
{
	private final char myOpenChar;
	private final char myCloseChar;

	public ExpressionOrStatementInsertHandler(char openChar, char closeChar)
	{
		myOpenChar = openChar;
		myCloseChar = closeChar;
	}

	@Override
	public void handleInsert(final InsertionContext context, final T item)
	{
		final Editor editor = context.getEditor();
		final Document document = editor.getDocument();
		context.commitDocument();
		PsiElement element = findNextToken(context);

		final char completionChar = context.getCompletionChar();

		final boolean putCaretInside = completionChar == myOpenChar || placeCaretInsideParentheses(context, item);

		if(completionChar == myOpenChar)
		{
			context.setAddCompletionChar(false);
		}

		boolean canAddSpaceBeforePair = canAddSpaceBeforePair(context, item);

		if(isToken(element, myOpenChar))
		{
			int lparenthOffset = element.getTextRange().getStartOffset();
			if(canAddSpaceBeforePair && lparenthOffset == context.getTailOffset())
			{
				document.insertString(context.getTailOffset(), " ");
				lparenthOffset++;
			}

			if(completionChar == myOpenChar || completionChar == '\t')
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
			if(isToken(last, myCloseChar))
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
					editor.getCaretModel().moveToOffset(lparenthOffset + 1);
				}
				return;
			}
		}
		else
		{
			document.insertString(context.getTailOffset(), getSpace(canAddSpaceBeforePair) + myOpenChar);
			editor.getCaretModel().moveToOffset(context.getTailOffset());
		}

		if(context.getCompletionChar() == myOpenChar)
		{
			int tail = context.getTailOffset();
			if(tail < document.getTextLength() && StringUtil.isJavaIdentifierPart(document.getCharsSequence().charAt(tail)))
			{
				return;
			}
		}

		document.insertString(context.getTailOffset(), String.valueOf(myCloseChar));
		if(!putCaretInside)
		{
			editor.getCaretModel().moveToOffset(context.getTailOffset());
		}
	}

	private static boolean isToken(@Nullable final PsiElement element, final char c)
	{
		if(element == null)
		{
			return false;
		}
		String text = element.getText();
		return text.length() == 1 && text.charAt(0) == c;
	}

	protected boolean placeCaretInsideParentheses(final InsertionContext context, final T item)
	{
		return true;
	}

	protected boolean canAddSpaceBeforePair(final InsertionContext insertionContext, final T item)
	{
		return false;
	}

	protected static CSharpCodeStyleSettings getCustomCodeStyleSettings(@NotNull InsertionContext context)
	{
		return CodeStyleSettingsManager.getSettings(context.getProject()).getCustomSettings(CSharpCodeStyleSettings.class);
	}

	private static String getSpace(boolean needSpace)
	{
		return needSpace ? " " :  "";
	}

	@Nullable
	protected PsiElement findNextToken(final InsertionContext context)
	{
		final PsiFile file = context.getFile();
		PsiElement element = file.findElementAt(context.getTailOffset());
		if(element instanceof PsiWhiteSpace)
		{
			if(element.getText().contains("\n"))
			{
				return null;
			}
			element = file.findElementAt(element.getTextRange().getEndOffset());
		}
		return element;
	}
}
