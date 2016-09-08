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
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokenSets;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpConstantBaseTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpPrefixExpressionImpl extends CSharpExpressionWithOperatorImpl implements DotNetExpression
{
	public static class PrefixTypeRef extends CSharpConstantBaseTypeRef
	{
		private CSharpPrefixExpressionImpl myPrefixExpression;

		public PrefixTypeRef(CSharpPrefixExpressionImpl prefixExpression, CSharpConstantExpressionImpl constantExpression, DotNetTypeRef delegate)
		{
			super(constantExpression, delegate);
			myPrefixExpression = prefixExpression;
		}

		@RequiredReadAction
		@NotNull
		@Override
		public String getPrefix()
		{
			IElementType operatorElementType = myPrefixExpression.getOperatorElement().getOperatorElementType();
			if(operatorElementType == CSharpTokenSets.MINUS)
			{
				return "-";
			}

			return "";
		}
	}

	public CSharpPrefixExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetTypeRef delegate = super.toTypeRefImpl(resolveFromParent);

		DotNetExpression expression = getExpression();
		if(!(expression instanceof CSharpConstantExpressionImpl))
		{
			return delegate;
		}

		return new PrefixTypeRef(this, (CSharpConstantExpressionImpl) expression, delegate);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitPrefixExpression(this);
	}

	@Nullable
	public DotNetExpression getExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}
}
