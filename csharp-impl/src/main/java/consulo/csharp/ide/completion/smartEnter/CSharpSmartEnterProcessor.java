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

package consulo.csharp.ide.completion.smartEnter;

import com.intellij.codeInsight.editorActions.smartEnter.SmartEnterProcessor;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetStatement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 03.01.15
 */
public class CSharpSmartEnterProcessor extends SmartEnterProcessor
{
	public interface Fixer
	{
		@RequiredReadAction
		boolean process(@Nonnull Editor editor, @Nonnull PsiFile psiFile);
	}

	public class FieldSemicolonFixer implements Fixer
	{
		@RequiredReadAction
		@Override
		public boolean process(@Nonnull Editor editor, @Nonnull PsiFile psiFile)
		{
			PsiElement statementAtCaret = getStatementAtCaret(editor, psiFile);
			DotNetFieldDeclaration variable = PsiTreeUtil.getParentOfType(statementAtCaret, DotNetFieldDeclaration.class);
			if(variable == null || variable.getNameIdentifier() == null)
			{
				return false;
			}

			ASTNode semicolonNode = variable.getNode().findChildByType(CSharpTokens.SEMICOLON);
			if(semicolonNode != null)
			{
				return false;
			}

			if(variable.hasModifier(CSharpModifier.ABSTRACT) || variable.hasModifier(CSharpModifier.PARTIAL) || variable.hasModifier(CSharpModifier.EXTERN))
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
		@RequiredReadAction
		@Override
		public boolean process(@Nonnull Editor editor, @Nonnull PsiFile psiFile)
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
			new FieldSemicolonFixer(),
			new StatementSemicolonFixer()
	};

	@Override
	@RequiredReadAction
	public boolean process(@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiFile psiFile)
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

	@RequiredReadAction
	private void insertStringAtEndWithReformat(@Nonnull String text, @Nonnull PsiElement anchor, @Nonnull Editor editor, int moveOffset, boolean commit)
	{
		PsiFile containingFile = anchor.getContainingFile();

		Document document = editor.getDocument();
		TextRange range = anchor.getTextRange();
		int endOffset = range.getEndOffset();
		document.insertString(endOffset, text);
		editor.getCaretModel().moveToOffset(endOffset + moveOffset);
		if(commit)
		{
			commit(editor);
		}

		reformatRange(containingFile, new TextRange(range.getStartOffset(), endOffset + moveOffset));
	}

	private void reformatRange(PsiFile psiFile, TextRange textRange) throws IncorrectOperationException
	{
		final PsiFile baseFile = psiFile.getViewProvider().getPsi(psiFile.getViewProvider().getBaseLanguage());
		CodeStyleManager.getInstance(psiFile.getProject()).reformatText(baseFile, textRange.getStartOffset(), textRange.getEndOffset());
	}
}
