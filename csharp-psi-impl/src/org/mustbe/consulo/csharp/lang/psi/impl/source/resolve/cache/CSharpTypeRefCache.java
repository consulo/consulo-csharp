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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache;

import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.NotNullLazyKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiModificationTracker;

/**
 * @author VISTALL
 * @since 5/11/2016
 */
@Deprecated
public class CSharpTypeRefCache
{
	private static final NotNullLazyKey<CSharpTypeRefCache, Project> ourInstanceKey = ServiceManager.createLazyKey(CSharpTypeRefCache.class);

	public static abstract class TypeRefResolver<TElement extends PsiElement>
	{
		@RequiredReadAction
		@NotNull
		public abstract DotNetTypeRef resolveTypeRef(@NotNull TElement ref, boolean resolveFromParent);
	}

	public static CSharpTypeRefCache getInstance(Project project)
	{
		ProgressIndicatorProvider.checkCanceled();
		return ourInstanceKey.getValue(project);
	}

	@SuppressWarnings("unchecked")
	private final ConcurrentMap<PsiElement, Getter<DotNetTypeRef>>[] myMaps = new ConcurrentMap[2];

	public CSharpTypeRefCache(Project project)
	{
		for(int i = 0; i < myMaps.length; i++)
		{
			myMaps[i] = CSharpResolveCache.createWeakMap();
		}

		project.getMessageBus().connect().subscribe(PsiModificationTracker.TOPIC, new PsiModificationTracker.Listener()
		{
			@Override
			public void modificationCountChanged()
			{
				for(ConcurrentMap<PsiElement, Getter<DotNetTypeRef>> map : myMaps)
				{
					map.clear();
				}
			}
		});
	}

	@RequiredReadAction
	@NotNull

	public <E extends PsiElement> DotNetTypeRef resolveTypeRef(E element, TypeRefResolver<E> typeRefResolver, boolean resolveFromParent)
	{
		ApplicationManager.getApplication().assertReadAccessAllowed();

		ConcurrentMap<PsiElement, Getter<DotNetTypeRef>> map = myMaps[resolveFromParent ? 1 : 0];

		Getter<DotNetTypeRef> getter = map.get(element);
		if(getter == null)
		{
			DotNetTypeRef typeRef = typeRefResolver.resolveTypeRef(element, resolveFromParent);
			map.putIfAbsent(element, new CSharpResolveCache.SoftGetter<DotNetTypeRef>(typeRef));
			return typeRef;
		}
		else
		{
			DotNetTypeRef typeRef = getter.get();
			assert typeRef != null;
			return typeRef;
		}
	}
}
