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
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 03.01.15
 */
public class CSharpSmartEnterProcessor extends SmartEnterProcessor
{
	public interface Fixer
	{
		boolean process(@NotNull Editor editor, @NotNull PsiFile psiFile);
	}

	public class VariableSemicolonFixer implements Fixer
	{

		@Override
		public boolean process(@NotNull Editor editor, @NotNull PsiFile psiFile)
		{
			PsiElement statementAtCaret = getStatementAtCaret(editor, psiFile);
			DotNetVariable variable = PsiTreeUtil.getParentOfType(statementAtCaret, DotNetVariable.class);
			if(variable == null || variable instanceof CSharpLocalVariable || variable.getNameIdentifier() == null)
			{
				return false;
			}

			ASTNode semicolonNode = variable.getNode().findChildByType(CSharpTokens.SEMICOLON);
			if(semicolonNode != null)
			{
				return false;
			}

			if(variable.hasModifier(CSharpModifier.ABSTRACT) || variable.hasModifier(CSharpModifier.PARTIAL) || variable.hasModifier(CSharpModifier
					.EXTERN))
			{
				insertStringAtEndWithReformat("();", variable, editor, 3, false);
			}
			else
			{
				insertStringAtEndWithReformat(";", variable, editor, 1, true);
			}
			return true;
		}
	}

	public class StatementSemicolonFixer implements Fixer
	{

		@Override
		public boolean process(@NotNull Editor editor, @NotNull PsiFile psiFile)
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
					insertStringAtEndWithReformat("{}", statement, editor, 1, true);
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

				insertStringAtEndWithReformat(";", statement, editor, 1, true);
			}
			return true;
		}
	}

	private Fixer[] myFixers = new Fixer[]{
			new VariableSemicolonFixer(),
			new StatementSemicolonFixer()
	};

	@Override
	public boolean process(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile psiFile)
	{
		for(Fixer fixer : myFixers)
		{
			if(fixer.process(editor, psiFile))
			{
				return true;
			}
		}
		return false;
	}

	private void insertStringAtEndWithReformat(@NotNull String text, @NotNull PsiElement anchor, @NotNull Editor editor, int moveOffset,
			boolean commit)
	{
		Document document = editor.getDocument();
		int endOffset = anchor.getTextRange().getEndOffset();
		document.insertString(endOffset, text);
		editor.getCaretModel().moveToOffset(endOffset + moveOffset);
		if(commit)
		{
			commit(editor);
		}
		reformat(anchor);
	}
}
