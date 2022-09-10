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
import consulo.language.psi.util.PsiTreeUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericWrapperTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 29.11.14
 */
public class CSharpLinqQueryBodyImpl extends CSharpElementImpl
{
	public CSharpLinqQueryBodyImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef calcTypeRef(boolean skipContinuation)
	{
		CSharpLinqSelectOrGroupClauseImpl selectOrGroupClause = getSelectOrGroupClause();
		if(selectOrGroupClause == null)
		{
			return DotNetTypeRef.ERROR_TYPE;
		}

		if(!skipContinuation)
		{
			CSharpLinqQueryContinuationImpl queryContinuation = getQueryContinuation();
			if(queryContinuation != null)
			{
				CSharpLinqQueryBodyImpl queryBody = queryContinuation.getQueryBody();
				if(queryBody != null)
				{
					return queryBody.calcTypeRef(false);
				}
			}
		}

		DotNetTypeRef innerTypeRef;
		if(selectOrGroupClause.isGroup())
		{
			DotNetTypeRef[] arguments = new DotNetTypeRef[] {typeRefOrError(selectOrGroupClause.getSecondExpression()),
					typeRefOrError(selectOrGroupClause.getFirstExpression())};
			innerTypeRef = new CSharpGenericWrapperTypeRef(getProject(), getResolveScope(), new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes2.System.Linq.IGrouping$2), arguments);
		}
		else
		{
			innerTypeRef = typeRefOrError(selectOrGroupClause.getFirstExpression());
		}

		if(innerTypeRef != DotNetTypeRef.ERROR_TYPE)
		{
			CSharpTypeRefByQName enumerableTypeRef = new CSharpTypeRefByQName(getProject(), getResolveScope(), DotNetTypes2.System.Collections.Generic.IEnumerable$1);
			return new CSharpGenericWrapperTypeRef(getProject(), getResolveScope(), enumerableTypeRef, innerTypeRef);
		}
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Nonnull
	private static DotNetTypeRef typeRefOrError(@Nullable DotNetExpression expression)
	{
		return expression == null ? DotNetTypeRef.ERROR_TYPE : expression.toTypeRef(true);
	}

	@Nullable
	public CSharpLinqSelectOrGroupClauseImpl getSelectOrGroupClause()
	{
		return findChildByClass(CSharpLinqSelectOrGroupClauseImpl.class);
	}

	@Nullable
	public CSharpLinqQueryContinuationImpl getQueryContinuation()
	{
		return findChildByClass(CSharpLinqQueryContinuationImpl.class);
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

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLinqQueryBody(this);
	}
}
