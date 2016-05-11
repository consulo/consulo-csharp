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
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpTypeRefCache;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public abstract class CSharpTypeElementImpl extends CSharpElementImpl implements DotNetType
{
	private static class OurResolver extends CSharpTypeRefCache.TypeRefResolver<CSharpTypeElementImpl>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@RequiredReadAction
		@NotNull
		@Override
		public DotNetTypeRef resolveTypeRef(@NotNull CSharpTypeElementImpl element, boolean resolveFromParent)
		{
			return element.toTypeRefImpl();
		}
	}

	public CSharpTypeElementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@RequiredReadAction
	public abstract DotNetTypeRef toTypeRefImpl();

	@RequiredReadAction
	@NotNull
	@Override
	public final DotNetTypeRef toTypeRef()
	{
		return CSharpTypeRefCache.getInstance(getProject()).resolveTypeRef(this, OurResolver.INSTANCE, true);
	}
}
