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
import org.mustbe.consulo.csharp.lang.psi.CSharpTokenSets;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpConstantTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpFastImplicitTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpPrefixExpressionImpl extends CSharpExpressionWithOperatorImpl implements DotNetExpression
{
	public static class PrefixTypeRef extends DotNetTypeRef.Delegate implements CSharpFastImplicitTypeRef
	{
		private CSharpPrefixExpressionImpl myPrefixExpression;

		public PrefixTypeRef(CSharpPrefixExpressionImpl prefixExpression, DotNetTypeRef delegate)
		{
			super(delegate);
			myPrefixExpression = prefixExpression;
		}

		@Nullable
		@Override
		public DotNetTypeRef doMirror(@NotNull DotNetTypeRef another, PsiElement scope)
		{
			DotNetExpression expression = myPrefixExpression.getExpression();
			if(!(expression instanceof CSharpConstantExpressionImpl))
			{
				return null;
			}

			DotNetTypeRef anotherTypeRef = CSharpConstantTypeRef.testNumberConstant((CSharpConstantExpressionImpl) expression,
					getPrefix(myPrefixExpression), another, scope);
			if(anotherTypeRef != null)
			{
				return anotherTypeRef;
			}
			return null;
		}

		@Override
		public boolean isConversion()
		{
			return false;
		}

		private static String getPrefix(CSharpPrefixExpressionImpl prefixExpression)
		{
			IElementType operatorElementType = prefixExpression.getOperatorElement().getOperatorElementType();
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
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		return new PrefixTypeRef(this, super.toTypeRef(resolveFromParent));
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
