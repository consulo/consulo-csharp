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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.lang.psi.impl.DotNetTypeRefCacheUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.Key;
import com.intellij.psi.util.CachedValue;
import com.intellij.util.NotNullFunction;

/**
 * @author VISTALL
 * @since 12-May-16
 */
public abstract class CSharpExpressionImpl extends CSharpElementImpl implements DotNetExpression
{
	private static Key<CachedValue<DotNetTypeRef>> ourTrueTypeRefKey = Key.create("CSharpExpressionImpl.ourTrueTypeRefKey");
	private static Key<CachedValue<DotNetTypeRef>> ourFalseTypeRefKey = Key.create("CSharpExpressionImpl.ourFalseTypeRefKey");

	private static class Resolver implements NotNullFunction<CSharpExpressionImpl, DotNetTypeRef>
	{
		private boolean myValue;

		private Resolver(boolean value)
		{
			myValue = value;
		}

		@NotNull
		@Override
		@RequiredReadAction
		public DotNetTypeRef fun(CSharpExpressionImpl expression)
		{
			return expression.toTypeRefImpl(myValue);
		}
	}

	private static final Resolver ourTrueResolver = new Resolver(true);
	private static final Resolver ourFalseResolver = new Resolver(false);

	public CSharpExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@RequiredReadAction
	public abstract DotNetTypeRef toTypeRefImpl(boolean resolveFromParent);

	@NotNull
	@Override
	@RequiredReadAction
	public final DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		Key<CachedValue<DotNetTypeRef>> key = resolveFromParent ? ourTrueTypeRefKey : ourFalseTypeRefKey;
		NotNullFunction<CSharpExpressionImpl, DotNetTypeRef> resolver = resolveFromParent ? ourTrueResolver : ourFalseResolver;
		return DotNetTypeRefCacheUtil.localCacheTypeRef(key, this, resolver);
	}
}
