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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.CSharpNullableTypeUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpNullCoalescingExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpNullCoalescingExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNullCoalescingExpression(this);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetExpression getCondition()
	{
		return (DotNetExpression) getFirstChild();
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getResult()
	{
		PsiElement[] children = getChildren();
		for(PsiElement child : children)
		{
			if(child == getCondition())
			{
				continue;
			}
			if(child instanceof DotNetExpression)
			{
				return (DotNetExpression) child;
			}
		}
		return null;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetExpression condition = getCondition();
		DotNetTypeRef typeRef = condition.toTypeRef(resolveFromParent);
		return CSharpNullableTypeUtil.unbox(typeRef);
	}
}
