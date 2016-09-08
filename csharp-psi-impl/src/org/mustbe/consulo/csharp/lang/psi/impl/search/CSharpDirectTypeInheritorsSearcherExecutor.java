/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package org.mustbe.consulo.csharp.lang.psi.impl.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.ExtendsListIndex;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.containers.HashMap;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.search.searches.DirectTypeInheritorsSearch;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;

/**
 * @author max
 *         <p/>
 *         Copied from Java plugin by Jetbrains (com.intellij.psi.search.searches.ClassInheritorsSearch)
 */
public class CSharpDirectTypeInheritorsSearcherExecutor implements QueryExecutor<DotNetTypeDeclaration, DirectTypeInheritorsSearch.SearchParameters>
{
	@Override
	public boolean execute(@NotNull final DirectTypeInheritorsSearch.SearchParameters p, @NotNull final Processor<DotNetTypeDeclaration> consumer)
	{
		String vmQName = p.getVmQName();

		/*if(DotNetTypes.System_Object.equals(qualifiedName))
		{
			final SearchScope scope = useScope;

			return AllClassesSearch.search(scope, aClass.getProject()).forEach(new Processor<DotNetTypeDeclaration>()
			{
				@Override
				public boolean process(final DotNetTypeDeclaration typeDcl)
				{
					if(typeDcl.isInterface())
					{
						return consumer.process(typeDcl);
					}
					final DotNetTypeDeclaration superClass = typeDcl.getSuperClass();
					if(superClass != null && DotNetTypes.System_Object.equals(ApplicationManager.getApplication().runReadAction(
							new Computable<String>()
					{
						public String compute()
						{
							return superClass.getPresentableQName();
						}
					})))
					{
						return consumer.process(typeDcl);
					}
					return true;
				}
			});
		}  */

		SearchScope useScope = p.getScope();
		final GlobalSearchScope scope = useScope instanceof GlobalSearchScope ? (GlobalSearchScope) useScope : new EverythingGlobalScope(p
				.getProject());
		final String searchKey = MsilHelper.cutGenericMarker(StringUtil.getShortName(vmQName));

		if(StringUtil.isEmpty(searchKey))
		{
			return true;
		}

		Collection<DotNetTypeList> candidates = ApplicationManager.getApplication().runReadAction(new Computable<Collection<DotNetTypeList>>()
		{
			@Override
			public Collection<DotNetTypeList> compute()
			{
				return ExtendsListIndex.getInstance().get(searchKey, p.getProject(), scope);
			}
		});

		Map<String, List<DotNetTypeDeclaration>> classes = new HashMap<String, List<DotNetTypeDeclaration>>();

		for(DotNetTypeList referenceList : candidates)
		{
			ProgressIndicatorProvider.checkCanceled();
			final DotNetTypeDeclaration candidate = (DotNetTypeDeclaration) referenceList.getParent();
			if(!checkInheritance(p, vmQName, candidate))
			{
				continue;
			}

			String fqn = ApplicationManager.getApplication().runReadAction(new Computable<String>()
			{
				@Override
				public String compute()
				{
					return candidate.getPresentableQName();
				}
			});
			List<DotNetTypeDeclaration> list = classes.get(fqn);
			if(list == null)
			{
				list = new ArrayList<DotNetTypeDeclaration>();
				classes.put(fqn, list);
			}
			list.add(candidate);
		}

		for(List<DotNetTypeDeclaration> sameNamedClasses : classes.values())
		{
			for(DotNetTypeDeclaration sameNamedClass : sameNamedClasses)
			{
				if(!consumer.process(sameNamedClass))
				{
					return false;
				}
			}
		}

		return true;
	}

	private static boolean checkInheritance(final DirectTypeInheritorsSearch.SearchParameters p,
			final String vmQName,
			final DotNetTypeDeclaration candidate)
	{
		return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>()
		{
			@Override
			public Boolean compute()
			{
				return !p.isCheckInheritance() || candidate.isInheritor(vmQName, false);
			}
		});
	}
}
