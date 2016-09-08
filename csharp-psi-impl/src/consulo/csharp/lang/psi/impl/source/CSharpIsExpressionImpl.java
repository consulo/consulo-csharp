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

package consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import com.intellij.lang.ASTNode;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpIsExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpIsExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@RequiredDispatchThread
	public DotNetTypeRef getIsTypeRef()
	{
		DotNetType type = findChildByClass(DotNetType.class);
		return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
	}

	@NotNull
	public DotNetExpression getExpression()
	{
		return findNotNullChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitIsExpression(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return new CSharpTypeRefByQName(this, DotNetTypes.System.Boolean);
	}
}
