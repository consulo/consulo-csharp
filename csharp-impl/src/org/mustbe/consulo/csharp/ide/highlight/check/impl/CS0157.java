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

package org.mustbe.consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFinallyStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
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

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Remove return statement";
		}

		@Override
		public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file)
		{
			return myPointer.getElement() != null;
		}

		@Override
		public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException
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
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpReturnStatementImpl element)
	{
		CSharpFinallyStatementImpl finallyStatement = PsiTreeUtil.getParentOfType(element, CSharpFinallyStatementImpl.class);
		if(finallyStatement != null)
		{
			return newBuilder(element).addQuickFix(new RemoveReturnStatementFix(element));
		}
		return null;
	}
}
