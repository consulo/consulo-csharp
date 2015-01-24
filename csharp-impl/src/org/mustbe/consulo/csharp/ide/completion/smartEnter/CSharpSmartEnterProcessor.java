/*
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

package org.mustbe.consulo.csharp.ide.completion.smartEnter;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 03.01.15
 */
public class CSharpSmartEnterProcessor extends SmartEnterProcessor
{
	@Override
	public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile)
	{
		PsiElement statementAtCaret = getStatementAtCaret(editor, psiFile);

		DotNetStatement statement = PsiTreeUtil.getParentOfType(statementAtCaret, DotNetStatement.class);
		if(statement == null)
		{
			return false;
		}

		if(statement instanceof CSharpBlockStatementImpl)
		{
			return false;
		}

		if(statement instanceof CSharpStatementAsStatementOwner)
		{
			DotNetStatement childStatement = ((CSharpStatementAsStatementOwner) statement).getChildStatement();

			if(childStatement == null)
			{
				Document document = editor.getDocument();
				int endOffset = statement.getTextRange().getEndOffset();
				document.insertString(endOffset, "{}");
				PsiDocumentManager.getInstance(project).commitDocument(document);
				editor.getCaretModel().moveToOffset(endOffset + 1);
				reformat(statement);
				return false;
			}
		}
		else
		{
			ASTNode node = statement.getNode();
			ASTNode semicolonNode = node.findChildByType(CSharpTokens.SEMICOLON);
			if(semicolonNode != null)
			{
				return false;
			}

			Document document = editor.getDocument();
			int endOffset = statement.getTextRange().getEndOffset();
			document.insertString(endOffset, ";");
			editor.getCaretModel().moveToOffset(endOffset + 1);
		}
		return true;
	}
}
