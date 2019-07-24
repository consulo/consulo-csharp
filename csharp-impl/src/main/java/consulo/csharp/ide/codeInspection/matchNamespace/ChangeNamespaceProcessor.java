/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.ide.codeInspection.matchNamespace;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewDescriptor;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.refactoring.move.CSharpClassesMoveProcessor;
import consulo.csharp.ide.refactoring.move.CSharpMoveClassesUtil;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2019-07-24
 */
public class ChangeNamespaceProcessor extends BaseRefactoringProcessor
{
	class UsageViewDescriptorImpl implements UsageViewDescriptor
	{
		@Nonnull
		@Override
		public PsiElement[] getElements()
		{
			return new PsiElement[] {myDeclaration};
		}

		@Override
		public String getProcessedElementsHeader()
		{
			return getCommandName();
		}

		@Override
		public String getCodeReferencesText(int usagesCount, int filesCount)
		{
			return null;
		}

		@Nullable
		@Override
		public String getCommentReferencesText(int usagesCount, int filesCount)
		{
			return null;
		}
	}

	@Nonnull
	private final CSharpNamespaceDeclaration myDeclaration;
	@Nonnull
	private final String myExpectedNamespace;

	protected ChangeNamespaceProcessor(@Nonnull Project project, @Nonnull CSharpNamespaceDeclaration declaration, @Nonnull String expectedNamespace)
	{
		super(project);
		myDeclaration = declaration;
		myExpectedNamespace = expectedNamespace;
	}

	@Nonnull
	@Override
	protected UsageViewDescriptor createUsageViewDescriptor(@Nonnull UsageInfo[] usages)
	{
		return new UsageViewDescriptorImpl();
	}

	@Nonnull
	@Override
	@RequiredReadAction
	protected UsageInfo[] findUsages()
	{
		List<UsageInfo> result = new ArrayList<>();
		Set<Couple<DotNetNamedElement>> children = CSharpMoveClassesUtil.findTypesAndNamespaces(myDeclaration);

		for(Couple<DotNetNamedElement> couple : children)
		{
			DotNetNamedElement second = couple.getSecond();

			for(PsiReference reference : ReferencesSearch.search(second, CSharpClassesMoveProcessor.mapScope(second)))
			{
				result.add(new CSharpClassesMoveProcessor.MyUsageInfo(reference.getElement(), couple, reference));
			}
		}

		return result.toArray(new UsageInfo[result.size()]);
	}

	@Override
	@RequiredWriteAction
	protected void performRefactoring(@Nonnull UsageInfo[] usages)
	{
		myDeclaration.setNamespace(myExpectedNamespace);

		CSharpClassesMoveProcessor.retargetUsages(usages);
	}

	@Nonnull
	@Override
	protected String getCommandName()
	{
		return "Change namespace to '" + myExpectedNamespace + "'";
	}
}
