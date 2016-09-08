/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide.navigation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.GotoClassContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;

/**
 * @author VISTALL
 * @since 15.12.13
 */
public class CSharpTypeNameContributor implements ChooseByNameContributorEx, GotoClassContributor
{
	@NotNull
	@Override
	public String[] getNames(Project project, boolean includeNonProjectItems)
	{
		CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<String>(ContainerUtil.<String>newTroveSet());
		processNames(processor, GlobalSearchScope.allScope(project), IdFilter.getProjectIdFilter(project, includeNonProjectItems));
		return processor.toArray(ArrayUtil.STRING_ARRAY_FACTORY);
	}

	@NotNull
	@Override
	@RequiredReadAction
	public NavigationItem[] getItemsByName(String name, final String pattern, Project project, boolean includeNonProjectItems)
	{
		CommonProcessors.CollectProcessor<NavigationItem> processor = new CommonProcessors.CollectProcessor<NavigationItem>(ContainerUtil
				.<NavigationItem>newTroveSet());
		processElementsWithName(name, processor, new FindSymbolParameters(pattern, name, GlobalSearchScope.allScope(project),
				IdFilter.getProjectIdFilter(project, includeNonProjectItems)));
		return processor.toArray(NavigationItem.ARRAY_FACTORY);
	}


	@Override
	public void processNames(@NotNull Processor<String> stringProcessor, @NotNull GlobalSearchScope searchScope, @Nullable IdFilter idFilter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, stringProcessor, searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, stringProcessor, searchScope, idFilter);
	}

	@Override
	public void processElementsWithName(@NotNull String name,
			@NotNull final Processor<NavigationItem> navigationItemProcessor,
			@NotNull FindSymbolParameters findSymbolParameters)
	{
		Project project = findSymbolParameters.getProject();
		IdFilter idFilter = findSymbolParameters.getIdFilter();
		Processor temp = navigationItemProcessor;
		GlobalSearchScope searchScope = findSymbolParameters.getSearchScope();

		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_INDEX, name, project, searchScope, idFilter, CSharpTypeDeclaration.class, temp);
		StubIndex.getInstance().processElements(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, name, project, searchScope, idFilter,
				CSharpMethodDeclaration.class, temp);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getQualifiedName(NavigationItem navigationItem)
	{
		if(navigationItem instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) navigationItem).getPresentableQName();
		}
		return null;
	}

	@Nullable
	@Override
	public String getQualifiedNameSeparator()
	{
		return ".";
	}
}
