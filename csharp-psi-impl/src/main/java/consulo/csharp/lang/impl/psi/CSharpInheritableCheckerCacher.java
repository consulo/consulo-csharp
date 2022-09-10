/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.lang.impl.psi;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.disposer.Disposable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.ide.ServiceManager;
import consulo.language.psi.PsiModificationTrackerListener;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;
import consulo.util.lang.Pair;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author VISTALL
 * @since 2020-10-24
 */
@Singleton
public class CSharpInheritableCheckerCacher implements Disposable
{
	public static CSharpInheritableCheckerCacher getInstance(@Nonnull Project project)
	{
		return ServiceManager.getService(project, CSharpInheritableCheckerCacher.class);
	}

	private static class CacheKey
	{
		private final DotNetTypeRef myTop;
		private final DotNetTypeRef myTarget;

		private final Pair<CSharpCastType, GlobalSearchScope> myCastTypeResolver;

		private CacheKey(DotNetTypeRef top, DotNetTypeRef target, Pair<CSharpCastType, GlobalSearchScope> castTypeResolver)
		{
			myTop = top;
			myTarget = target;
			myCastTypeResolver = castTypeResolver;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o)
			{
				return true;
			}
			if(o == null || getClass() != o.getClass())
			{
				return false;
			}
			CacheKey cacheKey = (CacheKey) o;
			return Objects.equals(myTop, cacheKey.myTop) &&
					Objects.equals(myTarget, cacheKey.myTarget) &&
					Objects.equals(myCastTypeResolver, cacheKey.myCastTypeResolver);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(myTop, myTarget, myCastTypeResolver);
		}

		@Override
		public String toString()
		{
			return "CacheKey{" +
					"myTop=" + myTop +
					", myTarget=" + myTarget +
					", myCastTypeResolver=" + myCastTypeResolver +
					'}';
		}
	}

	private final Map<CacheKey, CSharpTypeUtil.InheritResult> myCache = new ConcurrentHashMap<>();

	@Inject
	public CSharpInheritableCheckerCacher(Project project)
	{
		project.getMessageBus().connect(this).subscribe(PsiModificationTrackerListener.class, myCache::clear);
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTypeUtil.InheritResult getOrCheck(DotNetTypeRef top,
												   DotNetTypeRef target,
												   @Nullable Pair<CSharpCastType, GlobalSearchScope> castTypeResolver,
												   @Nullable CSharpInheritableCheckerContext context)
	{
		CacheKey key = new CacheKey(top, target, castTypeResolver);
		CSharpTypeUtil.InheritResult result = myCache.get(key);
		if(result != null)
		{
			return result;
		}

		result = CSharpInheritableChecker.isInheritable(key.myTop, key.myTarget, key.myCastTypeResolver, context == null ? CSharpInheritableCheckerContext.create() : context);
		myCache.putIfAbsent(key, result);
		return result;
	}

	@Override
	public void dispose()
	{
		myCache.clear();
	}
}
