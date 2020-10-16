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

import com.intellij.psi.tree.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 30.01.15
 */
public class CSharpRefValueExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpRefValueExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitRefValueExpression(this);
	}

	@Nullable
	public DotNetExpression getExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Nullable
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean b)
	{
		DotNetType type = getType();
		if(type == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}
		return type.toTypeRef();
	}
}
