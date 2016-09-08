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

package consulo.csharp.ide.codeInspection.unusedUsing;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class UnusedUsingInspection extends LocalInspectionTool
{
	public static final class DeleteStatement extends LocalQuickFixOnPsiElement
	{
		protected DeleteStatement(@NotNull PsiElement element)
		{
			super(element);
		}

		@NotNull
		@Override
		public String getText()
		{
			return "Delete statement";
		}

		@Override
		public void invoke(@NotNull Project project, @NotNull PsiFile psiFile, @NotNull final PsiElement element, @NotNull PsiElement element2)
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

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	private static final Key<UnusedUsingVisitor> KEY = Key.create("UnusedUsingVisitor");

	@NotNull
	@Override
	public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session)
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
	public void inspectionFinished(@NotNull LocalInspectionToolSession session, @NotNull ProblemsHolder problemsHolder)
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
}
