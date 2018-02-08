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
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpTypeCastExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpTypeCastExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTypeCastExpression(this);
	}

	@NotNull
	public DotNetType getType()
	{
		return findNotNullChildByClass(DotNetType.class);
	}

	@Nullable
	public DotNetExpression getInnerExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return getType().toTypeRef();
	}
}