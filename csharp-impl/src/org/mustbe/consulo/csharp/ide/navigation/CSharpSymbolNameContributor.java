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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.EventIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.FieldIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MethodIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.PropertyIndex;
import org.mustbe.consulo.dotnet.psi.DotNetEventDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetPropertyDeclaration;
import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;

/**
 * @author VISTALL
 * @since 21.12.13
 */
public class CSharpSymbolNameContributor implements ChooseByNameContributorEx
{
	@NotNull
	@Override
	public String[] getNames(Project project, boolean includeNonProjectItems)
	{
		Collection<String> k1 = MethodIndex.getInstance().getAllKeys(project);
		Collection<String> k2 = EventIndex.getInstance().getAllKeys(project);
		Collection<String> k3 = PropertyIndex.getInstance().getAllKeys(project);
		Collection<String> k4 = FieldIndex.getInstance().getAllKeys(project);
		List<String> list = new ArrayList<String>(k1.size() + k2.size() + k3.size() + k4.size());
		list.addAll(k1);
		list.addAll(k2);
		list.addAll(k3);
		list.addAll(k4);
		return ArrayUtil.toStringArray(list);
	}

	@NotNull
	@Override
	public NavigationItem[] getItemsByName(String name, final String pattern, Project project, boolean includeNonProjectItems)
	{
		Collection<? extends PsiElement> k1 = MethodIndex.getInstance().get(name, project, GlobalSearchScope.allScope(project));
		Collection<? extends PsiElement> k2 = EventIndex.getInstance().get(name, project, GlobalSearchScope.allScope(project));
		Collection<? extends PsiElement> k3 = PropertyIndex.getInstance().get(name, project, GlobalSearchScope.allScope(project));
		Collection<? extends PsiElement> k4 = FieldIndex.getInstance().get(name, project, GlobalSearchScope.allScope(project));

		List<PsiElement> list = new ArrayList<PsiElement>(k1.size() + k2.size() + k3.size() + k4.size());
		list.addAll(k1);
		list.addAll(k2);
		list.addAll(k3);
		list.addAll(k4);
		return list.toArray(new NavigationItem[list.size()]);
	}

	@Override
	public void processNames(@NotNull Processor<String> stringProcessor, @NotNull GlobalSearchScope searchScope, @Nullable IdFilter idFilter)
	{
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.METHOD_INDEX, stringProcessor, searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.EVENT_INDEX, stringProcessor, searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.PROPERTY_INDEX, stringProcessor, searchScope, idFilter);
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.FIELD_INDEX, stringProcessor, searchScope, idFilter);
	}

	@Override
	public void processElementsWithName(
			@NotNull String name, @NotNull Processor<NavigationItem> navigationItemProcessor, @NotNull FindSymbolParameters findSymbolParameters)
	{
		Project project = findSymbolParameters.getProject();
		IdFilter idFilter = findSymbolParameters.getIdFilter();
		GlobalSearchScope searchScope = findSymbolParameters.getSearchScope();

		StubIndex.getInstance().processElements(CSharpIndexKeys.METHOD_INDEX, name, project, searchScope, idFilter,
				DotNetLikeMethodDeclaration.class, (Processor) navigationItemProcessor);
		StubIndex.getInstance().processElements(CSharpIndexKeys.EVENT_INDEX, name, project, searchScope, idFilter,
				DotNetEventDeclaration.class,  (Processor) navigationItemProcessor);
		StubIndex.getInstance().processElements(CSharpIndexKeys.PROPERTY_INDEX, name, project, searchScope, idFilter,
				DotNetPropertyDeclaration.class, (Processor) navigationItemProcessor);
		StubIndex.getInstance().processElements(CSharpIndexKeys.FIELD_INDEX, name, project, searchScope, idFilter,
				DotNetFieldDeclaration.class, (Processor) navigationItemProcessor);
	}
}
