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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.ExtendsListIndex;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.psi.search.searches.DirectClassInheritorsSearch;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.ArchiveFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.HashMap;

/**
 * @author max
 *         <p/>
 *         Copied from Java plugin by Jetbrains (com.intellij.psi.search.searches.ClassInheritorsSearch)
 */
public class CSharpDirectInheritorsSearcherExecutor implements QueryExecutor<DotNetTypeDeclaration, DirectClassInheritorsSearch.SearchParameters>
{
	@Override
	public boolean execute(@NotNull final DirectClassInheritorsSearch.SearchParameters p, @NotNull final Processor<DotNetTypeDeclaration> consumer)
	{
		final DotNetTypeDeclaration aClass = p.getClassToProcess();
		final PsiManagerImpl psiManager = (PsiManagerImpl) aClass.getManager();

		final SearchScope useScope = ApplicationManager.getApplication().runReadAction(new Computable<SearchScope>()
		{
			@Override
			public SearchScope compute()
			{
				return aClass.getUseScope();
			}
		});

		final String qualifiedName = ApplicationManager.getApplication().runReadAction(new Computable<String>()
		{
			@Override
			public String compute()
			{
				return aClass.getPresentableQName();
			}
		});

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

		final GlobalSearchScope scope = useScope instanceof GlobalSearchScope ? (GlobalSearchScope) useScope : new EverythingGlobalScope(psiManager
				.getProject());
		final String searchKey = ApplicationManager.getApplication().runReadAction(new Computable<String>()
		{
			@Override
			public String compute()
			{
				return aClass.getName();
			}
		});
		if(StringUtil.isEmpty(searchKey))
		{
			return true;
		}

		Collection<DotNetTypeList> candidates = ApplicationManager.getApplication().runReadAction(new Computable<Collection<DotNetTypeList>>()
		{
			@Override
			public Collection<DotNetTypeList> compute()
			{
				return ExtendsListIndex.getInstance().get(searchKey, psiManager.getProject(), scope);
			}
		});

		Map<String, List<DotNetTypeDeclaration>> classes = new HashMap<String, List<DotNetTypeDeclaration>>();

		for(DotNetTypeList referenceList : candidates)
		{
			ProgressIndicatorProvider.checkCanceled();
			final DotNetTypeDeclaration candidate = (DotNetTypeDeclaration) referenceList.getParent();
			if(!checkInheritance(p, aClass, candidate))
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
			if(!processSameNamedClasses(consumer, aClass, sameNamedClasses))
			{
				return false;
			}
		}

		return true;
	}

	private static boolean checkInheritance(final DirectClassInheritorsSearch.SearchParameters p, final DotNetTypeDeclaration aClass,
			final DotNetTypeDeclaration candidate)
	{
		return ApplicationManager.getApplication().runReadAction(new Computable<Boolean>()
		{
			@Override
			public Boolean compute()
			{
				return !p.isCheckInheritance() || candidate.isInheritor(aClass, false);
			}
		});
	}

	private static boolean processSameNamedClasses(Processor<DotNetTypeDeclaration> consumer, DotNetTypeDeclaration aClass,
			List<DotNetTypeDeclaration> sameNamedClasses)
	{
		// if there is a class from the same jar, prefer it
		boolean sameJarClassFound = false;

		VirtualFile jarFile = getArchiveFile(aClass);
		if(jarFile != null)
		{
			for(DotNetTypeDeclaration sameNamedClass : sameNamedClasses)
			{
				boolean fromSameJar = Comparing.equal(getArchiveFile(sameNamedClass), jarFile);
				if(fromSameJar)
				{
					sameJarClassFound = true;
					if(!consumer.process(sameNamedClass))
					{
						return false;
					}
				}
			}
		}

		return sameJarClassFound || ContainerUtil.process(sameNamedClasses, consumer);
	}

	@Nullable
	public static VirtualFile getArchiveFile(@NotNull PsiElement candidate)
	{
		VirtualFile file = candidate.getContainingFile().getVirtualFile();
		if(file != null && file.getFileSystem() instanceof ArchiveFileSystem)
		{
			return VfsUtilCore.getVirtualFileForJar(file);
		}
		return file;
	}
}
