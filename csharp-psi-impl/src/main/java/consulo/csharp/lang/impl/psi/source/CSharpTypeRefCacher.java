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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValue;
import consulo.dotnet.psi.impl.DotNetTypeRefCacheUtil;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.util.dataholder.Key;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 13-May-16
 */
public abstract class CSharpTypeRefCacher<E extends PsiElement>
{
	private static class Resolver<E extends PsiElement> implements Function<E, DotNetTypeRef>
	{
		private CSharpTypeRefCacher<E> myCacher;
		private boolean myValue;

		private Resolver(CSharpTypeRefCacher<E> cacher, boolean value)
		{
			myCacher = cacher;
			myValue = value;
		}

		@Nonnull
		@Override
		@RequiredReadAction
		public DotNetTypeRef apply(E e)
		{
			return myCacher.toTypeRefImpl(e, myValue);
		}
	}

	private static Key<CachedValue<DotNetTypeRef>> ourTrueTypeRefKey = Key.create("CSharpTypeCacheUtil.ourTrueTypeRefKey");
	private static Key<CachedValue<DotNetTypeRef>> ourFalseTypeRefKey = Key.create("CSharpTypeCacheUtil.ourFalseTypeRefKey");

	private Function<E, DotNetTypeRef> myTrueFunction = new Resolver<>(this, true);
	private Function<E, DotNetTypeRef> myFalseFunction = new Resolver<>(this, false);

	private final boolean myLocal;

	protected CSharpTypeRefCacher(boolean local)
	{
		myLocal = local;
	}

	@Nonnull
	@RequiredReadAction
	protected abstract DotNetTypeRef toTypeRefImpl(E element, boolean resolveFromParentOrInitializer);

	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef toTypeRef(E element, boolean resolveFromParentOrInitializer)
	{
		if(CSharpReferenceExpressionImplUtil.isCacheDisabled(element))
		{
			return toTypeRefImpl(element, resolveFromParentOrInitializer);
		}

		Key<CachedValue<DotNetTypeRef>> key = resolveFromParentOrInitializer ? ourTrueTypeRefKey : ourFalseTypeRefKey;
		Function<E, DotNetTypeRef> resolver = resolveFromParentOrInitializer ? myTrueFunction : myFalseFunction;
		return myLocal ? DotNetTypeRefCacheUtil.localCacheTypeRef(key, element, resolver) : DotNetTypeRefCacheUtil.cacheTypeRef(key, element, resolver);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef toTypeRef(E element, boolean resolveFromParentOrInitializer, Object... dropKeys)
	{
		if(CSharpReferenceExpressionImplUtil.isCacheDisabled(element))
		{
			return toTypeRefImpl(element, resolveFromParentOrInitializer);
		}

		Key<CachedValue<DotNetTypeRef>> key = resolveFromParentOrInitializer ? ourTrueTypeRefKey : ourFalseTypeRefKey;
		Function<E, DotNetTypeRef> resolver = resolveFromParentOrInitializer ? myTrueFunction : myFalseFunction;
		return DotNetTypeRefCacheUtil.cacheTypeRef(key, element, resolver, dropKeys);
	}
}
