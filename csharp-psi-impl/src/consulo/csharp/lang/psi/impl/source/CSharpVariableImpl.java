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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public abstract class CSharpVariableImpl extends CSharpMemberImpl implements DotNetVariable
{
	private static final CSharpTypeRefCacher<CSharpVariableImpl> ourCacheSystem = new CSharpTypeRefCacher<CSharpVariableImpl>(true)
	{
		@RequiredReadAction
		@NotNull
		@Override
		protected DotNetTypeRef toTypeRefImpl(CSharpVariableImpl element, boolean resolveFromParentOrInitializer)
		{
			return element.toTypeRefImpl(resolveFromParentOrInitializer);
		}
	};

	private final ThreadLocal<Boolean> myTypeRefProcessing = ThreadLocal.withInitial(() -> Boolean.FALSE);

	public CSharpVariableImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return CSharpTypeRefCacher.ENABLED ? ourCacheSystem.toTypeRef(this, resolveFromInitializer) : toTypeRefImpl(resolveFromInitializer);
	}

	@RequiredReadAction
	@NotNull
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		if(type == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		DotNetTypeRef runtimeType = type.toTypeRef();
		if(resolveFromInitializer && runtimeType == DotNetTypeRef.AUTO_TYPE)
		{
			final DotNetExpression initializer = getInitializer();
			if(initializer == null)
			{
				return DotNetTypeRef.ERROR_TYPE;
			}

			if(myTypeRefProcessing.get())
			{
				return DotNetTypeRef.AUTO_TYPE;
			}

			try
			{
				myTypeRefProcessing.set(Boolean.TRUE);
				return initializer.toTypeRef(true);
			}
			finally
			{
				myTypeRefProcessing.set(Boolean.FALSE);
			}
		}
		else
		{
			return runtimeType;
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getConstantKeywordElement()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean isConstant()
	{
		return false;
	}
}
