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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.util.ArrayUtil2;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 29.11.14
 */
public class CSharpLinqSelectOrGroupClauseImpl extends CSharpElementImpl
{
	public CSharpLinqSelectOrGroupClauseImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public boolean isGroup()
	{
		return findChildByType(CSharpSoftTokens.GROUP_KEYWORD) != null;
	}

	@Nullable
	public DotNetExpression getFirstExpression()
	{
		DotNetExpression[] expressions = findChildrenByClass(DotNetExpression.class);
		return ArrayUtil2.safeGet(expressions, 0);
	}

	@Nullable
	public DotNetExpression getSecondExpression()
	{
		DotNetExpression[] expressions = findChildrenByClass(DotNetExpression.class);
		return ArrayUtil2.safeGet(expressions, 1);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLinqSelectOrGroupClause(this);
	}
}
