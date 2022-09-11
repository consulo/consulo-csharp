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

package consulo.csharp.impl.ide.codeInspection.unnecessarySemicolon;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInspection.CSharpGeneralLocalInspection;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpEmptyStatementImpl;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 5/9/2016
 */
@ExtensionImpl
public class UnnecessarySemicolonInspection extends CSharpGeneralLocalInspection
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

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Unnecessary semicolon";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}
}
