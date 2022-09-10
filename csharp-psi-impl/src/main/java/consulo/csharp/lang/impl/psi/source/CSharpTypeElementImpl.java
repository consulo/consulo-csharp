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
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.impl.DotNetTypeRefCacheUtil;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public abstract class CSharpTypeElementImpl extends CSharpElementImpl implements DotNetType
{
	private static class Resolver implements Function<CSharpTypeElementImpl, DotNetTypeRef>
	{
		public static final Resolver INSTANCE = new Resolver();

		public DotNetTypeRef apply(CSharpTypeElementImpl typeElement)
		{
			return typeElement.toTypeRefImpl();
		}
	}

	public CSharpTypeElementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	@RequiredReadAction
	protected abstract DotNetTypeRef toTypeRefImpl();

	@RequiredReadAction
	@Nonnull
	@Override
	public final DotNetTypeRef toTypeRef()
	{
		return DotNetTypeRefCacheUtil.localCacheTypeRef(this, Resolver.INSTANCE);
	}
}
