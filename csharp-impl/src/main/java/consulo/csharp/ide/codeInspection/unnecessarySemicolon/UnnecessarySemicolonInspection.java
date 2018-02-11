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

package consulo.csharp.ide.codeInspection.unnecessarySemicolon;

import javax.annotation.Nonnull;

import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpEmptyStatementImpl;
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
		private RemoveSemicolonFix(@Nonnull PsiElement element)
		{
			super(element);
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Remove unnecessary semicolon";
		}

		@Override
		public void invoke(@Nonnull Project project, @Nonnull PsiFile file, @Nonnull PsiElement startElement, @Nonnull PsiElement endElement)
		{
			startElement.delete();
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly)
	{
		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitEmptyStatement(CSharpEmptyStatementImpl statement)
			{
				PsiElement parent = statement.getParent();
				if(parent instanceof CSharpBlockStatementImpl)
				{
					holder.registerProblem(statement, null, "Unnecessary Semicolon", new RemoveSemicolonFix(statement));
				}
			}
		};
	}
}
