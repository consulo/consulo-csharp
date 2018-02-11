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

package consulo.csharp.lang.psi.impl.partial;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import com.intellij.ProjectTopics;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootAdapter;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.util.Factory;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;

/**
 * @author VISTALL
 * @since 02.05.2015
 */
public class CSharpPartialElementManager implements Disposable
{
	@Nonnull
	public static CSharpPartialElementManager getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, CSharpPartialElementManager.class);
	}

	private final Map<GlobalSearchScope, Map<String, CSharpTypeDeclaration>> myCache = ContainerUtil.createConcurrentWeakMap();
	private final Project myProject;

	public CSharpPartialElementManager(@Nonnull Project project)
	{
		myProject = project;
		project.getMessageBus().connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener.Adapter()
		{
			@Override
			public void beforePsiChanged(boolean isPhysical)
			{
				myCache.clear();
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

	@Nonnull
	public CSharpTypeDeclaration getOrCreateCompositeType(@Nonnull final GlobalSearchScope scope, @Nonnull final String vmQName, @Nonnull final Collection<CSharpTypeDeclaration> typeDeclarations)
	{
		Map<String, CSharpTypeDeclaration> scopeMap = ContainerUtil.getOrCreate(myCache, scope, (Factory<Map<String, CSharpTypeDeclaration>>) ContainerUtil::createConcurrentWeakValueMap);

		return ContainerUtil.getOrCreate(scopeMap, vmQName, (Factory<CSharpTypeDeclaration>) () -> new CSharpCompositeTypeDeclaration(myProject, scope, ContainerUtil.toArray(typeDeclarations,
				CSharpTypeDeclaration.ARRAY_FACTORY)));
	}

	@Override
	public void dispose()
	{
		myCache.clear();
	}
}
