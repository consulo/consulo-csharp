/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.partial;

import java.util.Collection;
import java.util.Map;

import consulo.lombok.annotations.ProjectService;
import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 02.05.2015
 */
@ProjectService
public class CSharpPartialElementManager implements Disposable
{
	private final Map<GlobalSearchScope, Map<String, CSharpTypeDeclaration>> myCache = ContainerUtil.createConcurrentWeakMap();
	private long myOutOfCodeModification;
	private final Project myProject;

	public CSharpPartialElementManager(@NotNull Project project, @NotNull final PsiModificationTracker modificationTracker)
	{
		myProject = project;
		project.getMessageBus().connect().subscribe(PsiModificationTracker.TOPIC, new PsiModificationTracker.Listener()
		{
			@Override
			public void modificationCountChanged()
			{
				long l = modificationTracker.getOutOfCodeBlockModificationCount();
				if(l != myOutOfCodeModification)
				{
					myOutOfCodeModification = l;
					myCache.clear();
				}
			}
		});

		project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootAdapter()
		{
			@Override
			public void rootsChanged(ModuleRootEvent event)
			{
				myCache.clear();
			}
		});
	}

	@NotNull
	public CSharpTypeDeclaration getOrCreateCompositeType(@NotNull final GlobalSearchScope scope, @NotNull final String vmQName, @NotNull final Collection<CSharpTypeDeclaration> typeDeclarations)
	{
		Map<String, CSharpTypeDeclaration> scopeMap = ContainerUtil.getOrCreate(myCache, scope, new Factory<Map<String, CSharpTypeDeclaration>>()
		{
			@Override
			public Map<String, CSharpTypeDeclaration> create()
			{
				return ContainerUtil.createConcurrentWeakValueMap();
			}
		});

		return ContainerUtil.getOrCreate(scopeMap, vmQName, new Factory<CSharpTypeDeclaration>()
		{
			@Override
			public CSharpTypeDeclaration create()
			{
				return new CSharpCompositeTypeDeclaration(myProject, scope, ContainerUtil.toArray(typeDeclarations, CSharpTypeDeclaration.ARRAY_FACTORY));
			}
		});
	}

	@Override
	public void dispose()
	{
		myCache.clear();
	}
}
