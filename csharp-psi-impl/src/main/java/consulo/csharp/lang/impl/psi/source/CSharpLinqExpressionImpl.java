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

import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.ast.IElementType;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLinqExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpLinqExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	public CSharpLinqFromClauseImpl getFromClause()
	{
		return findNotNullChildByClass(CSharpLinqFromClauseImpl.class);
	}

	@Nullable
	public CSharpLinqQueryBodyImpl getQueryBody()
	{
		return findChildByClass(CSharpLinqQueryBodyImpl.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLinqExpression(this);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor,
			@Nonnull ResolveState state,
			PsiElement lastParent,
			@Nonnull PsiElement place)
	{
		if(lastParent == null || !PsiTreeUtil.isAncestor(this, lastParent, false))
		{
			return true;
		}

		for(PsiElement psiElement : getChildren())
		{
			if(!psiElement.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		CSharpLinqQueryBodyImpl queryBody = getQueryBody();
		if(queryBody == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		return queryBody.calcTypeRef(false);
	}
}
