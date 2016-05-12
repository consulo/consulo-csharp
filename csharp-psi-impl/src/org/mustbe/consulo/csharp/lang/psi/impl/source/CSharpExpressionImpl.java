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
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import consulo.csharp.lang.psi.impl.source.CSharpTypeRefCacher;

/**
 * @author VISTALL
 * @since 12-May-16
 */
public abstract class CSharpExpressionImpl extends CSharpElementImpl implements DotNetExpression
{
	private static final CSharpTypeRefCacher<CSharpExpressionImpl> ourCacheSystem = new CSharpTypeRefCacher<CSharpExpressionImpl>(true)
	{
		@RequiredReadAction
		@NotNull
		@Override
		protected DotNetTypeRef toTypeRefImpl(CSharpExpressionImpl element, boolean resolveFromParentOrInitializer)
		{
			return element.toTypeRefImpl(resolveFromParentOrInitializer);
		}
	};

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
		return ourCacheSystem.toTypeRef(this, resolveFromParent);
	}
}
