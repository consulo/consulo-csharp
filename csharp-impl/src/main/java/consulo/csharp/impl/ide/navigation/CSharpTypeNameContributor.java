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

package consulo.csharp.impl.ide.navigation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.ide.navigation.GotoClassOrTypeContributor;
import consulo.language.psi.search.FindSymbolParameters;
import consulo.language.psi.stub.IdFilter;
import consulo.language.psi.stub.StubIndex;
import consulo.navigation.NavigationItem;
import consulo.project.Project;
import consulo.project.content.scope.ProjectAwareSearchScope;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13
 */
@ExtensionImpl
public class CSharpTypeNameContributor implements GotoClassOrTypeContributor
{
	@Override
	public void processNames(@Nonnull Processor<String> stringProcessor, @Nonnull SearchScope searchScope, @Nullable IdFilter idFilter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, stringProcessor, (ProjectAwareSearchScope)searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, stringProcessor, (ProjectAwareSearchScope)searchScope, idFilter);
	}

	@Override
	public void processElementsWithName(@Nonnull String name, @Nonnull final Processor<NavigationItem> navigationItemProcessor, @Nonnull FindSymbolParameters findSymbolParameters)
	{
		Project project = findSymbolParameters.getProject();
		IdFilter idFilter = findSymbolParameters.getIdFilter();
		Processor temp = navigationItemProcessor;
		ProjectAwareSearchScope searchScope = findSymbolParameters.getSearchScope();

		StubIndex.getInstance().processElements(CSharpIndexKeys.TYPE_INDEX, name, project, searchScope, idFilter, CSharpTypeDeclaration.class, temp);
		StubIndex.getInstance().processElements(CSharpIndexKeys.DELEGATE_METHOD_BY_NAME_INDEX, name, project, searchScope, idFilter, CSharpMethodDeclaration.class, temp);
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
