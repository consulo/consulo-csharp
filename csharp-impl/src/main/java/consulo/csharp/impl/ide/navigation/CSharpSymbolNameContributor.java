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

import consulo.annotation.component.ExtensionImpl;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.stub.index.CSharpIndexKeys;
import consulo.dotnet.psi.DotNetEventDeclaration;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.ide.navigation.GotoSymbolContributor;
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
 * @since 21.12.13
 */
@ExtensionImpl
public class CSharpSymbolNameContributor implements GotoSymbolContributor
{
	@Override
	public void processNames(@Nonnull Processor<String> stringProcessor, @Nonnull SearchScope searchScope, @Nullable IdFilter idFilter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.METHOD_INDEX, stringProcessor, (ProjectAwareSearchScope) searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.EVENT_INDEX, stringProcessor, (ProjectAwareSearchScope) searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.PROPERTY_INDEX, stringProcessor, (ProjectAwareSearchScope) searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.FIELD_INDEX, stringProcessor, (ProjectAwareSearchScope) searchScope, idFilter);
	}

	@Override
	public void processElementsWithName(@Nonnull String name, @Nonnull Processor<NavigationItem> navigationItemProcessor, @Nonnull FindSymbolParameters findSymbolParameters)
	{
		Project project = findSymbolParameters.getProject();
		IdFilter idFilter = findSymbolParameters.getIdFilter();
		ProjectAwareSearchScope searchScope = findSymbolParameters.getSearchScope();

		StubIndex.getInstance().processElements(CSharpIndexKeys.METHOD_INDEX, name, project, searchScope, idFilter,
				DotNetLikeMethodDeclaration.class, (Processor) navigationItemProcessor);
		StubIndex.getInstance().processElements(CSharpIndexKeys.EVENT_INDEX, name, project, searchScope, idFilter,
				DotNetEventDeclaration.class, (Processor) navigationItemProcessor);
		StubIndex.getInstance().processElements(CSharpIndexKeys.PROPERTY_INDEX, name, project, searchScope, idFilter,
				DotNetPropertyDeclaration.class, (Processor) navigationItemProcessor);
		StubIndex.getInstance().processElements(CSharpIndexKeys.FIELD_INDEX, name, project, searchScope, idFilter,
				DotNetFieldDeclaration.class, (Processor) navigationItemProcessor);
	}
}
