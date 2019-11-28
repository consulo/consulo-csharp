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

package consulo.csharp.ide.highlight.check.impl;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.impl.source.CSharpFinallyStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 01.01.15
 */
public class CS0157 extends CompilerCheck<CSharpReturnStatementImpl>
{
	public static class RemoveReturnStatementFix extends BaseIntentionAction
	{
		private SmartPsiElementPointer<CSharpReturnStatementImpl> myPointer;

		public RemoveReturnStatementFix(CSharpReturnStatementImpl declaration)
		{
			myPointer = SmartPointerManager.getInstance(declaration.getProject()).createSmartPsiElementPointer(declaration);
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Remove return statement";
		}

		@Override
		public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@Nonnull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
		{
			CSharpReturnStatementImpl element = myPointer.getElement();
			if(element == null)
			{
				return;
			}
			element.delete();
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull CSharpReturnStatementImpl element)
	{
		CSharpFinallyStatementImpl finallyStatement = PsiTreeUtil.getParentOfType(element, CSharpFinallyStatementImpl.class);
		if(finallyStatement != null)
		{
			return newBuilder(element).addQuickFix(new RemoveReturnStatementFix(element));
		}
		return null;
	}
}
