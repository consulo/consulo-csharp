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

package consulo.csharp.lang.impl.psi.partial;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.disposer.Disposable;
import consulo.language.psi.AnyPsiChangeListener;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.content.layer.event.ModuleRootAdapter;
import consulo.module.content.layer.event.ModuleRootEvent;
import consulo.module.content.layer.event.ModuleRootListener;
import consulo.project.Project;
import consulo.util.collection.ContainerUtil;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

/**
 * @author VISTALL
 * @since 02.05.2015
 */
@Singleton
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class CSharpPartialElementManager implements Disposable
{
	@Nonnull
	public static CSharpPartialElementManager getInstance(@Nonnull Project project)
	{
		return project.getInstance(CSharpPartialElementManager.class);
	}

	private final Map<GlobalSearchScope, Map<String, CSharpTypeDeclaration>> myCache = ContainerUtil.createConcurrentWeakMap();
	private final Project myProject;

	@Inject
	public CSharpPartialElementManager(@Nonnull Project project)
	{
		myProject = project;
		project.getMessageBus().connect().subscribe(AnyPsiChangeListener.class, new AnyPsiChangeListener()
		{
			@Override
			public void beforePsiChanged(boolean isPhysical)
			{
				myCache.clear();
			}
		});

		project.getMessageBus().connect().subscribe(ModuleRootListener.class, new ModuleRootAdapter()
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
		Map<String, CSharpTypeDeclaration> scopeMap = myCache.computeIfAbsent(scope, (c) -> ContainerUtil.createConcurrentWeakValueMap());

		return scopeMap.computeIfAbsent(vmQName, (c) -> new CSharpCompositeTypeDeclaration(myProject, scope, ContainerUtil.toArray(typeDeclarations,
				CSharpTypeDeclaration.ARRAY_FACTORY)));
	}

	@Override
	public void dispose()
	{
		myCache.clear();
	}
}
