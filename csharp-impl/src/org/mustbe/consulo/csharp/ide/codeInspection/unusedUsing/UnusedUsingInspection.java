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

package org.mustbe.consulo.csharp.ide.codeInspection.unusedUsing;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 05.03.2016
 */
public class UnusedUsingInspection extends LocalInspectionTool
{
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

			problemsHolder.registerProblem(entry.getKey(), "Using statement is not used", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
		}
	}
}
