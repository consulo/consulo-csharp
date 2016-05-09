/*
 * Copyright 2013-2016 must-be.org
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

package org.mustbe.consulo.csharp.ide.codeInspection.unnecessarySemicolon;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpEmptyStatementImpl;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 5/9/2016
 */
public class UnnecessarySemicolonInspection extends LocalInspectionTool
{
	private static class RemoveSemicolonFix extends LocalQuickFixOnPsiElement
	{
		private RemoveSemicolonFix(@NotNull PsiElement element)
		{
			super(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Remove unnecessary semicolon";
		}

		@Override
		public void invoke(@NotNull Project project, @NotNull PsiFile file, @NotNull PsiElement startElement, @NotNull PsiElement endElement)
		{
			startElement.delete();
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitEmptyStatement(CSharpEmptyStatementImpl statement)
			{
				holder.registerProblem(statement, null, "Unnecessary Semicolon", new RemoveSemicolonFix(statement));
			}
		};
	}
}
