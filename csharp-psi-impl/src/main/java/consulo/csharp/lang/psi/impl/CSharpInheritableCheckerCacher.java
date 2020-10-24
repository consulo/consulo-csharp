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

package consulo.csharp.lang.psi.impl;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.disposer.Disposable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.util.lang.Pair;
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
		private final Boolean myDisableNullableCheck;

		private CacheKey(DotNetTypeRef top, DotNetTypeRef target, Pair<CSharpCastType, GlobalSearchScope> castTypeResolver, Boolean disableNullableCheck)
		{
			myTop = top;
			myTarget = target;
			myCastTypeResolver = castTypeResolver;
			myDisableNullableCheck = disableNullableCheck;
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
					Objects.equals(myCastTypeResolver, cacheKey.myCastTypeResolver) &&
					Objects.equals(myDisableNullableCheck, cacheKey.myDisableNullableCheck);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(myTop, myTarget, myCastTypeResolver, myDisableNullableCheck);
		}

		@Override
		public String toString()
		{
			return "CacheKey{" +
					"myTop=" + myTop +
					", myTarget=" + myTarget +
					", myCastTypeResolver=" + myCastTypeResolver +
					", myDisableNullableCheck=" + myDisableNullableCheck +
					'}';
		}
	}

	private final Map<CacheKey, CSharpTypeUtil.InheritResult> myCache = new ConcurrentHashMap<>();

	public CSharpInheritableCheckerCacher(Project project)
	{
		project.getMessageBus().connect(this).subscribe(PsiModificationTracker.TOPIC, () ->
		{
			myCache.clear();
		});
	}

	@Nonnull
	@RequiredReadAction
	public CSharpTypeUtil.InheritResult getOrCheck(DotNetTypeRef top, DotNetTypeRef target, @Nullable Pair<CSharpCastType, GlobalSearchScope> castTypeResolver, Boolean disableNullableCheck)
	{
		CacheKey key = new CacheKey(top, target, castTypeResolver, disableNullableCheck == Boolean.TRUE);
		CSharpTypeUtil.InheritResult result = myCache.get(key);
		if(result != null)
		{
			return result;
		}

		result = CSharpInheritableChecker.isInheritable(key.myTop, key.myTarget, key.myCastTypeResolver, key.myDisableNullableCheck);
		myCache.putIfAbsent(key, result);
		return result;
	}

	@Override
	public void dispose()
	{
		myCache.clear();
	}
}
