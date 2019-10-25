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

package consulo.csharp.ide.navigation;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.containers.ContainerUtil;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 17-May-16
 */
public class CSharpMethodImplementationsSearcher implements QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters>
{
	@Override
	public boolean execute(@Nonnull DefinitionsScopedSearch.SearchParameters queryParameters, @Nonnull Processor<? super PsiElement> consumer)
	{
		PsiElement element = queryParameters.getElement();
		if(element instanceof DotNetVirtualImplementOwner)
		{
			Collection<DotNetVirtualImplementOwner> members = ReadAction.compute(() -> OverrideUtil.collectOverridenMembers((DotNetVirtualImplementOwner) element));
			return ContainerUtil.process(members, consumer);
		}
		return true;
	}
}
