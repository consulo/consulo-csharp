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
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpConditionalExpressionImpl extends CSharpElementImpl implements DotNetExpression
{
	public CSharpConditionalExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitConditionalExpression(this);
	}

	private DotNetExpression[] getExpressions()
	{
		return findChildrenByClass(DotNetExpression.class);
	}

	@NotNull
	public DotNetExpression getCondition()
	{
		return ArrayUtil2.safeGet(getExpressions(), 0);
	}

	@Nullable
	public DotNetExpression getTrueExpression()
	{
		return ArrayUtil2.safeGet(getExpressions(), 1);
	}

	@Nullable
	public DotNetExpression getFalseExpression()
	{
		return ArrayUtil2.safeGet(getExpressions(), 2);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		DotNetExpression trueExpression = getTrueExpression();
		DotNetExpression falseExpression = getFalseExpression();
		if(falseExpression == null && trueExpression != null)
		{
			return trueExpression.toTypeRef(resolveFromParent);
		}
		if(trueExpression == null && falseExpression == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(trueExpression == null)
		{
			return falseExpression.toTypeRef(resolveFromParent);
		}

		DotNetTypeRef trueType = trueExpression.toTypeRef(resolveFromParent);
		DotNetTypeRef falseType = falseExpression.toTypeRef(resolveFromParent);
		if(CSharpTypeUtil.isInheritable(falseType, trueType, this))
		{
			return trueType;
		}
		else
		{
			return falseType;
		}
	}
}
