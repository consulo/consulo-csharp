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

import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTokenSets;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpConstantBaseTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpBinaryExpressionImpl extends CSharpExpressionWithOperatorImpl implements DotNetExpression, CSharpCallArgumentListOwner
{
	private static class BinaryTypeRef extends CSharpConstantBaseTypeRef
	{
		BinaryTypeRef(CSharpConstantExpressionImpl leftExpression, DotNetTypeRef delegate)
		{
			super(leftExpression, delegate);
		}
	}

	public CSharpBinaryExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitBinaryExpression(this);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		DotNetTypeRef delegate = super.toTypeRefImpl(resolveFromParent);

		IElementType operatorElementType = getOperatorElement().getOperatorElementType();
		if(operatorElementType == CSharpTokenSets.LTLT || operatorElementType == CSharpTokenSets.GTGT)
		{
			DotNetExpression leftExpression = getLeftExpression();
			if(leftExpression instanceof CSharpConstantExpressionImpl)
			{
				return new BinaryTypeRef((CSharpConstantExpressionImpl) leftExpression, delegate);
			}
			else if(leftExpression instanceof CSharpPrefixExpressionImpl)
			{
				DotNetExpression expression = ((CSharpPrefixExpressionImpl) leftExpression).getExpression();
				if(expression instanceof CSharpConstantExpressionImpl)
				{
					return new CSharpPrefixExpressionImpl.PrefixTypeRef((CSharpPrefixExpressionImpl)leftExpression, (CSharpConstantExpressionImpl)expression, delegate);
				}
			}
		}
		return delegate;
	}

	@Nullable
	public DotNetExpression getLeftExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getRightExpression()
	{
		PsiElement operatorElement = getOperatorElement();
		PsiElement nextSibling = operatorElement.getNextSibling();
		while(nextSibling != null)
		{
			if(nextSibling instanceof DotNetExpression)
			{
				return (DotNetExpression) nextSibling;
			}
			nextSibling = nextSibling.getNextSibling();
		}
		return null;
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetExpression leftExpression = getLeftExpression();
		if(leftExpression != null && !leftExpression.processDeclarations(processor, state, lastParent, place))
		{
			return false;
		}

		DotNetExpression rightExpression = getRightExpression();
		if(rightExpression != null && !rightExpression.processDeclarations(processor, state, lastParent, place))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
