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

package org.mustbe.consulo.csharp.ide.navigation;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 17-May-16
 */
public class CSharpMethodImplementationsSearcher implements QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters>
{
	@Override
	public boolean execute(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<PsiElement> consumer)
	{
		PsiElement element = queryParameters.getElement();
		if(element instanceof DotNetVirtualImplementOwner)
		{
			Collection<DotNetVirtualImplementOwner> members = OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) element);
			return ContainerUtil.process(members, consumer);
		}
		return true;
	}
}
