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

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.ast.IElementType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpWhileStatementImpl extends CSharpElementImpl implements DotNetStatement, CSharpStatementAsStatementOwner
{
	public CSharpWhileStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nullable
	public DotNetExpression getConditionExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitWhileStatement(this);
	}

	@Nullable
	@Override
	public DotNetStatement getChildStatement()
	{
		return findChildByClass(DotNetStatement.class);
	}
}
