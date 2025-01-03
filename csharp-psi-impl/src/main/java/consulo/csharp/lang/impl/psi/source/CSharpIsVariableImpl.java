/*
 * Copyright 2013-2019 consulo.io
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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-09-28
 */
public class CSharpIsVariableImpl extends CSharpVariableImpl
{
	public CSharpIsVariableImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitIsVariable(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		CSharpIsExpressionImpl expression = getExpression();
		return expression.getIsTypeRef();
	}

	@Nonnull
	public CSharpIsExpressionImpl getExpression()
	{
		return (CSharpIsExpressionImpl) getParent();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return null;
	}
}
