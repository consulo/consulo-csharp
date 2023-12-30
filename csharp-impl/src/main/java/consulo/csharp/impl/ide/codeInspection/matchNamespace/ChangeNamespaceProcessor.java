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

package consulo.csharp.impl.ide.codeInspection.matchNamespace;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.impl.ide.refactoring.move.CSharpClassesMoveProcessor;
import consulo.csharp.impl.ide.refactoring.move.CSharpMoveClassesUtil;
import consulo.csharp.lang.psi.CSharpNamespaceProvider;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.language.editor.refactoring.BaseRefactoringProcessor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiReference;
import consulo.language.psi.search.ReferencesSearch;
import consulo.project.Project;
import consulo.usage.UsageInfo;
import consulo.usage.UsageViewDescriptor;
import consulo.util.lang.Couple;

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
			return new PsiElement[] {myNamespaceProvider};
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
	private final CSharpNamespaceProvider myNamespaceProvider;
	@Nonnull
	private final String myExpectedNamespace;

	protected ChangeNamespaceProcessor(@Nonnull Project project, @Nonnull CSharpNamespaceProvider namespaceProvider, @Nonnull String expectedNamespace)
	{
		super(project);
		myNamespaceProvider = namespaceProvider;
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
		Set<Couple<DotNetNamedElement>> children = CSharpMoveClassesUtil.findTypesAndNamespaces(myNamespaceProvider);

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
		myNamespaceProvider.setNamespace(myExpectedNamespace);

		CSharpClassesMoveProcessor.retargetUsages(usages);
	}

	@Nonnull
	@Override
	protected String getCommandName()
	{
		return "Change namespace to '" + myExpectedNamespace + "'";
	}
}
