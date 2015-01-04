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

package org.mustbe.consulo.csharp.ide.actions;

import javax.swing.JTextArea;

import org.mustbe.consulo.csharp.ide.controlFlow.CSharpControlFlow;
import org.mustbe.consulo.csharp.ide.controlFlow.CSharpControlFlowBuilder;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TextComponentUndoProvider;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class DebugControlFlowAction extends AnAction
{
	@Override
	public void actionPerformed(AnActionEvent anActionEvent)
	{
		DotNetQualifiedElement element = findElement(anActionEvent);
		if(element == null)
		{
			return;
		}

		CSharpControlFlow build = CSharpControlFlowBuilder.build(element);

		showTextAreaDialog(build.toDebugString(), "Control Flow", "#CSharpControlFlow");
	}

	@Override
	public void update(AnActionEvent e)
	{
		e.getPresentation().setEnabledAndVisible(findElement(e) != null);
	}

	private static DotNetQualifiedElement findElement(AnActionEvent e)
	{
		PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
		if(psiFile == null)
		{
			return null;
		}

		Caret caret = e.getData(CommonDataKeys.CARET);
		if(caret == null)
		{
			return null;
		}

		PsiElement elementAt = psiFile.findElementAt(caret.getOffset());

		return PsiTreeUtil.getParentOfType(elementAt, DotNetQualifiedElement.class);
	}

	private static void showTextAreaDialog(String text, String title, String dimensionServiceKey)
	{
		final JTextArea textArea = new JTextArea(10, 50);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setText(StringUtil.join(text, "\n"));
		final DialogBuilder builder = new DialogBuilder();
		builder.setDimensionServiceKey(dimensionServiceKey);
		builder.setCenterPanel(ScrollPaneFactory.createScrollPane(textArea));
		builder.setPreferredFocusComponent(textArea);
		String rawText = title;
		if(StringUtil.endsWithChar(rawText, ':'))
		{
			rawText = rawText.substring(0, rawText.length() - 1);
		}
		builder.setTitle(rawText);
		builder.addOkAction();
		builder.addCancelAction();
		builder.setOkOperation(new Runnable()
		{
			@Override
			public void run()
			{
				builder.getDialogWrapper().close(DialogWrapper.OK_EXIT_CODE);
			}
		});
		builder.addDisposable(new TextComponentUndoProvider(textArea));
		builder.show();
	}
}
