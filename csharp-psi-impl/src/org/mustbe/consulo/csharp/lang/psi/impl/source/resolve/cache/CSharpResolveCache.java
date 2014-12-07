/*
 * Copyright 2000-2009 JetBrains s.r.o.
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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.RecursionGuard;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.openapi.util.StaticGetter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.AnyPsiChangeListener;
import com.intellij.psi.impl.PsiManagerImpl;
import com.intellij.reference.SoftReference;
import com.intellij.util.containers.ConcurrentWeakHashMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.messages.MessageBus;

/**
 * @author VISTALl
 * @since 18:06 26.10.2014
 * <p/>
 * This is variant of {@link com.intellij.psi.impl.source.resolve.ResolveCache} with 'resolveFromParent' and 'incompleteCode'
 */
public class CSharpResolveCache
{
	private static final Logger LOG = Logger.getInstance("#com.intellij.psi.impl.source.resolve.ResolveCache");
	private final ConcurrentMap[] myMaps = new ConcurrentMap[2 * 2 * 2 * 2]; //boolean physical, boolean incompleteCode,
	// boolean resolveFromParent, boolean isPoly
	private final RecursionGuard myGuard = RecursionManager.createGuard("csharpResolveCache");

	public static CSharpResolveCache getInstance(Project project)
	{
		ProgressIndicatorProvider.checkCanceled(); // We hope this method is being called often enough to cancel daemon processes smoothly
		return ServiceManager.getService(project, CSharpResolveCache.class);
	}

	public interface AbstractResolver<TRef extends PsiElement, TResult>
	{
		TResult resolve(@NotNull TRef ref, boolean incompleteCode, boolean resolveFromParent);
	}

	public interface PolyVariantResolver<T extends PsiElement> extends AbstractResolver<T, ResolveResult[]>
	{
		@Override
		@NotNull
		ResolveResult[] resolve(@NotNull T t, boolean incompleteCode, boolean resolveFromParent);
	}

	public static abstract class TypeRefResolver<TElement extends PsiElement> implements AbstractResolver<TElement, DotNetTypeRef>
	{
		@Override
		@NotNull
		public final DotNetTypeRef resolve(@NotNull TElement ref, boolean incompleteCode, boolean resolveFromParent)
		{
			return resolveTypeRef(ref, resolveFromParent);
		}

		@NotNull
		public abstract DotNetTypeRef resolveTypeRef(@NotNull TElement element, boolean resolveFromParent);
	}

	public interface Resolver extends AbstractResolver<PsiElement, PsiElement>
	{
	}

	public CSharpResolveCache(@NotNull MessageBus messageBus)
	{
		for(int i = 0; i < myMaps.length; i++)
		{
			myMaps[i] = createWeakMap();
		}
		messageBus.connect().subscribe(PsiManagerImpl.ANY_PSI_CHANGE_TOPIC, new AnyPsiChangeListener()
		{
			@Override
			public void beforePsiChanged(boolean isPhysical)
			{
				clearCache(isPhysical);
			}

			@Override
			public void afterPsiChanged(boolean isPhysical)
			{
			}
		});
	}

	private static <K, V> ConcurrentWeakHashMap<K, V> createWeakMap()
	{
		return new ConcurrentWeakHashMap<K, V>(100, 0.75f, Runtime.getRuntime().availableProcessors(), ContainerUtil.<K>canonicalStrategy());
	}

	public void clearCache(boolean isPhysical)
	{
		int startIndex = isPhysical ? 0 : 1;
		for(int i = startIndex; i < 2; i++)
		{
			for(int j = 0; j < 2; j++)
			{
				for(int k = 0; k < 2; k++)
				{
					myMaps[i * 4 + j * 2 + k].clear();
				}
			}
		}
	}

	@Nullable
	private <TRef extends PsiElement, TResult> TResult resolve(@NotNull final TRef ref,
			@NotNull final AbstractResolver<TRef, TResult> resolver,
			boolean needToPreventRecursion,
			final boolean incompleteCode,
			final boolean resolveFromParent,
			boolean isPoly,
			boolean isPhysical)
	{
		ProgressIndicatorProvider.checkCanceled();
		ApplicationManager.getApplication().assertReadAccessAllowed();

		ConcurrentMap<TRef, Getter<TResult>> map = getMap(isPhysical, incompleteCode, resolveFromParent, isPoly);
		Getter<TResult> reference = map.get(ref);
		TResult result = reference == null ? null : reference.get();
		if(result != null)
		{
			return result;
		}

		RecursionGuard.StackStamp stamp = myGuard.markStack();
		result = needToPreventRecursion ? myGuard.doPreventingRecursion(Quaternary.create(ref, incompleteCode, resolveFromParent, isPoly), true,
				new Computable<TResult>()
		{
			@Override
			public TResult compute()
			{
				return resolver.resolve(ref, incompleteCode, resolveFromParent);
			}
		}) : resolver.resolve(ref, incompleteCode, resolveFromParent);
		PsiElement element = result instanceof ResolveResult ? ((ResolveResult) result).getElement() : null;
		LOG.assertTrue(element == null || element.isValid(), result);

		if(stamp.mayCacheNow())
		{
			cache(ref, map, result, isPoly);
		}
		return result;
	}

	@NotNull
	public <T extends PsiElement> ResolveResult[] resolveWithCaching(@NotNull T ref,
			@NotNull PolyVariantResolver<T> resolver,
			boolean needToPreventRecursion,
			boolean incompleteCode,
			boolean resolveFromParent)
	{
		return resolveWithCaching(ref, resolver, needToPreventRecursion, incompleteCode, resolveFromParent, ref.getContainingFile());
	}

	@NotNull
	public <T extends PsiElement> ResolveResult[] resolveWithCaching(@NotNull T ref,
			@NotNull PolyVariantResolver<T> resolver,
			boolean needToPreventRecursion,
			boolean incompleteCode,
			boolean resolveFromParent,
			@NotNull PsiFile containingFile)
	{
		ResolveResult[] result = resolve(ref, resolver, needToPreventRecursion, incompleteCode, resolveFromParent, true,
				containingFile.isPhysical());
		return result == null ? ResolveResult.EMPTY_ARRAY : result;
	}

	@Nullable
	public <T extends PsiPolyVariantReference & PsiElement> ResolveResult[] getCachedResults(@NotNull T ref,
			boolean physical,
			boolean incompleteCode,
			boolean resolveFromParent,
			boolean isPoly)
	{
		Map<T, Getter<ResolveResult[]>> map = getMap(physical, incompleteCode, resolveFromParent, isPoly);
		Getter<ResolveResult[]> reference = map.get(ref);
		return reference == null ? null : reference.get();
	}

	@Nullable
	public <TRef extends PsiElement, TResult> TResult resolveWithCaching(@NotNull TRef ref,
			@NotNull AbstractResolver<TRef, TResult> resolver,
			boolean needToPreventRecursion,
			boolean incompleteCode,
			boolean resolveFromParent)
	{
		return resolve(ref, resolver, needToPreventRecursion, incompleteCode, resolveFromParent, false, ref.isPhysical());
	}

	@NotNull
	public <TElement extends PsiElement> DotNetTypeRef resolveTypeRef(@NotNull TElement ref,
			@NotNull TypeRefResolver<TElement> resolver,
			boolean resolveFromParent)
	{
		DotNetTypeRef resolve = resolve(ref, resolver, true, false, resolveFromParent, false, ref.isPhysical());
		if(resolve == null)
		{
			// if is recursive call - return error
			return DotNetTypeRef.ERROR_TYPE;
		}
		return resolve;
	}

	private <TRef extends PsiElement, TResult> ConcurrentMap<TRef, Getter<TResult>> getMap(boolean physical,
			boolean incompleteCode,
			boolean resolveFromParent,
			boolean isPoly)
	{
		//noinspection unchecked
		return myMaps[(physical ? 0 : 1) * 8 + (incompleteCode ? 0 : 1) * 4 + (resolveFromParent ? 0 : 1) * 2 + (isPoly ? 0 : 1)];
	}

	private static class SoftGetter<T> extends SoftReference<T> implements Getter<T>
	{
		public SoftGetter(T referent)
		{
			super(referent);
		}
	}

	private static final Getter<ResolveResult[]> EMPTY_POLY_RESULT = new StaticGetter<ResolveResult[]>(ResolveResult.EMPTY_ARRAY);
	private static final Getter<Object> NULL_RESULT = new StaticGetter<Object>(null);

	private static <TRef extends PsiElement, TResult> void cache(@NotNull TRef ref,
			@NotNull ConcurrentMap<TRef, Getter<TResult>> map,
			TResult result,
			boolean isPoly)
	{
		// optimization: less contention
		Getter<TResult> cached = map.get(ref);
		if(cached != null && cached.get() == result)
		{
			return;
		}
		if(result == null)
		{
			// no use in creating SoftReference to null
			//noinspection unchecked
			cached = (Getter<TResult>) NULL_RESULT;
		}
		else if(isPoly && ((Object[]) result).length == 0)
		{
			// no use in creating SoftReference to empty array
			//noinspection unchecked
			cached = result.getClass() == ResolveResult[].class ? (Getter<TResult>) EMPTY_POLY_RESULT : new StaticGetter<TResult>(result);
		}
		else
		{
			cached = new SoftGetter<TResult>(result);
		}
		map.put(ref, cached);
	}
}
