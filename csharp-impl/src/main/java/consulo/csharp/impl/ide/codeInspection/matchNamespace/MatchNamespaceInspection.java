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

package consulo.csharp.impl.ide.codeInspection.matchNamespace;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.language.editor.inspection.LocalInspectionTool;
import consulo.language.editor.inspection.LocalInspectionToolSession;
import consulo.language.editor.inspection.ProblemsHolder;
import consulo.language.psi.PsiCodeFragment;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 05-Nov-17
 */
public abstract class MatchNamespaceInspection extends LocalInspectionTool
{
	private static final Key<MatchNamespaceVisitor> KEY = Key.create("MatchNamespaceVisitor");

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiElementVisitor buildVisitor(@Nonnull ProblemsHolder holder, boolean isOnTheFly, @Nonnull LocalInspectionToolSession session)
	{
		PsiFile file = holder.getFile();
		if(!(file instanceof CSharpFile))
		{
			return PsiElementVisitor.EMPTY_VISITOR;
		}

		if(file instanceof PsiCodeFragment)
		{
			return PsiElementVisitor.EMPTY_VISITOR;
		}

		DotNetSimpleModuleExtension extension = ModuleUtilCore.getExtension(file, DotNetSimpleModuleExtension.class);
		if(extension == null)
		{
			return PsiElementVisitor.EMPTY_VISITOR;
		}

		MatchNamespaceVisitor visitor = session.getUserData(KEY);
		if(visitor == null)
		{
			session.putUserData(KEY, visitor = new MatchNamespaceVisitor(holder, extension));
		}
		return visitor;
	}

	@Override
	public void inspectionFinished(@Nonnull LocalInspectionToolSession session, @Nonnull ProblemsHolder problemsHolder)
	{
		MatchNamespaceVisitor visitor = session.getUserData(KEY);
		if(visitor == null)
		{
			return;
		}
		visitor.report();
	}
}
