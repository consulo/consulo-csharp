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

package consulo.csharp.impl.ide.codeInspection.unusedUsing;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInspection.CSharpGeneralLocalInspection;
import consulo.csharp.lang.impl.ide.codeInspection.unusedUsing.UnusedUsingVisitor;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.language.editor.WriteCommandAction;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.LocalQuickFixOnPsiElement;
import consulo.language.editor.inspection.ProblemHighlightType;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.editor.rawHighlight.HighlightDisplayLevel;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.project.Project;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
@ExtensionImpl
public class UnusedUsingInspection extends CSharpGeneralLocalInspection
{
	public static final class DeleteStatement extends LocalQuickFixOnPsiElement
	{
		protected DeleteStatement(@Nonnull PsiElement element)
		{
			super(element);
		}

		@Nonnull
		@Override
		public String getText()
		{
			return "Delete statement";
		}

		@Override
		public void invoke(@Nonnull Project project, @Nonnull PsiFile psiFile, @Nonnull final PsiElement element, @Nonnull PsiElement element2)
		{
			new WriteCommandAction.Simple<Object>(project, psiFile)
			{
				@Override
				protected void run() throws Throwable
				{
					element.delete();
				}
			}.execute();
		}

		@Nonnull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	private static final Key<UnusedUsingVisitor> KEY = Key.create("UnusedUsingVisitor");

	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly, @Nonnull LocalInspectionToolSession session, @Nonnull Object state)
	{
		UnusedUsingVisitor visitor = session.getUserData(KEY);
		if(visitor == null)
		{
			session.putUserData(KEY, visitor = new UnusedUsingVisitor());
		}
		return visitor;
	}

	@Override
	@RequiredReadAction
	public void inspectionFinished(@Nonnull LocalInspectionToolSession session, @Nonnull ProblemsHolder problemsHolder, @Nonnull Object state)
	{
		UnusedUsingVisitor visitor = session.getUserData(KEY);
		if(visitor == null)
		{
			return;
		}

		Map<CSharpUsingListChild, Boolean> usingContext = visitor.getUsingContext();
		for(Map.Entry<CSharpUsingListChild, Boolean> entry : usingContext.entrySet())
		{
			if(entry.getValue())
			{
				continue;
			}

			CSharpUsingListChild element = entry.getKey();
			problemsHolder.registerProblem(element, "Using statement is not used", ProblemHighlightType.LIKE_UNUSED_SYMBOL, new DeleteStatement(element));
		}
	}

	@Nonnull
	@Override
	public String getDisplayName()
	{
		return "Unused using";
	}

	@Nonnull
	@Override
	public HighlightDisplayLevel getDefaultLevel()
	{
		return HighlightDisplayLevel.WARNING;
	}
}
