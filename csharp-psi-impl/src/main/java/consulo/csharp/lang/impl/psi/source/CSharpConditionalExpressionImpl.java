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

import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpTypeUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpNullTypeRef;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpConditionalExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpConditionalExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConditionalExpression(this);
	}

	private DotNetExpression[] getExpressions()
	{
		return findChildrenByClass(DotNetExpression.class);
	}

	@Nonnull
	public DotNetExpression getConditionExpression()
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

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
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
		if(trueType instanceof CSharpNullTypeRef)
		{
			return falseType;
		}

		if(falseType instanceof CSharpNullTypeRef)
		{
			return trueType;
		}

		if(CSharpTypeUtil.isInheritableWithImplicit(falseType, trueType, getResolveScope()))
		{
			return trueType;
		}
		else
		{
			return falseType;
		}
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetExpression conditionExpression = getConditionExpression();
		if(!conditionExpression.processDeclarations(processor, state, lastParent, place))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
