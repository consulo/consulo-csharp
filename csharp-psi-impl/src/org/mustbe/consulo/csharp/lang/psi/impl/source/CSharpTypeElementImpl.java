/*
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpResolveCache;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy.CSharpLazyTypeRefWrapper;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public abstract class CSharpTypeElementImpl extends CSharpElementImpl implements DotNetType
{
	private static class OurResolver extends CSharpResolveCache.TypeRefResolver<CSharpTypeElementImpl>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		public DotNetTypeRef resolveTypeRef(@NotNull CSharpTypeElementImpl element, boolean resolveFromParent)
		{
			DotNetTypeRef delegate = element.toTypeRefImpl();
			if(delegate == DotNetTypeRef.AUTO_TYPE || delegate == DotNetTypeRef.UNKNOWN_TYPE || delegate == DotNetTypeRef.ERROR_TYPE || delegate ==
					CSharpStaticTypeRef.EXPLICIT || delegate == CSharpStaticTypeRef.IMPLICIT || delegate == CSharpStaticTypeRef.DYNAMIC || delegate
					== CSharpStaticTypeRef.__ARGLIST_TYPE)
			{
				return delegate;
			}
			return new CSharpLazyTypeRefWrapper(element, delegate);
		}
	}

	public CSharpTypeElementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	public abstract DotNetTypeRef toTypeRefImpl();

	@NotNull
	@Override
	public final DotNetTypeRef toTypeRef()
	{
		return CSharpResolveCache.getInstance(getProject()).resolveTypeRef(this, OurResolver.INSTANCE, true);
	}
}
