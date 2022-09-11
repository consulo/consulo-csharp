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
package consulo.csharp.lang.impl.psi.search;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.ReadAction;
import consulo.application.progress.ProgressIndicatorProvider;
import consulo.application.util.function.Processor;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.stub.index.ExtendsListIndex;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeList;
import consulo.dotnet.psi.search.searches.DirectTypeInheritorsSearch;
import consulo.dotnet.psi.search.searches.DirectTypeInheritorsSearchExecutor;
import consulo.internal.dotnet.msil.decompiler.util.MsilHelper;
import consulo.language.psi.scope.EverythingGlobalScope;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * @author max
 *         <p/>
 *         Copied from Java plugin by Jetbrains (com.intellij.psi.search.searches.ClassInheritorsSearch)
 */
@ExtensionImpl
public class CSharpDirectTypeInheritorsSearcherExecutor implements DirectTypeInheritorsSearchExecutor
{
	@Override
	public boolean execute(@Nonnull final DirectTypeInheritorsSearch.SearchParameters p, @Nonnull final Processor<? super DotNetTypeDeclaration> consumer)
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

		int hashKey = searchKey.hashCode();

		Collection<DotNetTypeList> candidates = ReadAction.compute(() -> ExtendsListIndex.getInstance().get(hashKey, p.getProject(), scope));

		Map<String, List<DotNetTypeDeclaration>> classes = new HashMap<>();

		for(DotNetTypeList referenceList : candidates)
		{
			ProgressIndicatorProvider.checkCanceled();
			final DotNetTypeDeclaration candidate = ReadAction.compute(() -> (DotNetTypeDeclaration) referenceList.getParent());
			if(!checkInheritance(p, vmQName, candidate))
			{
				continue;
			}

			String fqn = ReadAction.compute(candidate::getPresentableQName);
			List<DotNetTypeDeclaration> list = classes.get(fqn);
			if(list == null)
			{
				list = new ArrayList<>();
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
		return ReadAction.compute(() -> !p.isCheckInheritance() || candidate.isInheritor(vmQName, false));
	}
}
