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

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeIndex;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.GotoClassContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
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
		Collection<String> allKeys = TypeIndex.getInstance().getAllKeys(project);
		return allKeys.toArray(new String[allKeys.size()]);
	}

	@NotNull
	@Override
	public NavigationItem[] getItemsByName(String name, final String pattern, Project project, boolean includeNonProjectItems)
	{
		Collection<DotNetTypeDeclaration> cSharpTypeDeclarations = TypeIndex.getInstance().get(name, project, GlobalSearchScope.allScope(project));
		NavigationItem[] items = new NavigationItem[cSharpTypeDeclarations.size()];
		int i = 0;
		for(DotNetTypeDeclaration t : cSharpTypeDeclarations)
		{
			items[i ++] = (NavigationItem) MsilToCSharpUtil.wrap(t);
		}
		return items;
	}

	@Override
	public void processNames(@NotNull Processor<String> stringProcessor, @NotNull GlobalSearchScope searchScope, @Nullable IdFilter idFilter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, stringProcessor, searchScope, idFilter);
	}

	@Override
	public void processElementsWithName(@NotNull String name, @NotNull final Processor<NavigationItem> navigationItemProcessor,
			@NotNull FindSymbolParameters findSymbolParameters)
	{
		Project project = findSymbolParameters.getProject();
		IdFilter idFilter = findSymbolParameters.getIdFilter();
		Processor<DotNetTypeDeclaration> castVar = new Processor<DotNetTypeDeclaration>()
		{
			@Override
			public boolean process(DotNetTypeDeclaration typeDeclaration)
			{
				return navigationItemProcessor.process((NavigationItem) MsilToCSharpUtil.wrap(typeDeclaration));
			}
		};
		GlobalSearchScope searchScope = findSymbolParameters.getSearchScope();

		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_INDEX, name, project, searchScope, idFilter, DotNetTypeDeclaration.class, castVar);
	}

	@Nullable
	@Override
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
