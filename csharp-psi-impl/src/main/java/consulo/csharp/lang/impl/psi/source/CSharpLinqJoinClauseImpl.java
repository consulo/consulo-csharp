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
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 30.11.14
 */
public class CSharpLinqJoinClauseImpl extends CSharpElementImpl
{
	public CSharpLinqJoinClauseImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nullable
	public CSharpLinqVariableImpl getVariable()
	{
		return findChildByClass(CSharpLinqVariableImpl.class);
	}

	@Nullable
	public DotNetExpression getInExpression()
	{
		DotNetExpression[] expressions = findChildrenByClass(DotNetExpression.class);
		return ArrayUtil2.safeGet(expressions, 0);
	}

	@Nullable
	public DotNetExpression getOnExpression()
	{
		DotNetExpression[] expressions = findChildrenByClass(DotNetExpression.class);
		return ArrayUtil2.safeGet(expressions, 1);
	}

	@Nullable
	public DotNetExpression getEqualsExpression()
	{
		DotNetExpression[] expressions = findChildrenByClass(DotNetExpression.class);
		return ArrayUtil2.safeGet(expressions, 2);
	}

	@Nullable
	public CSharpLinqIntoClauseImpl getIntoClause()
	{
		return findChildByClass(CSharpLinqIntoClauseImpl.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLinqJoinClause(this);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor,
			@Nonnull ResolveState state,
			PsiElement lastParent,
			@Nonnull PsiElement place)
	{
		CSharpLinqVariableImpl variable = getVariable();
		if(variable != null)
		{
			if(!processor.execute(variable, state))
			{
				return false;
			}
		}

		CSharpLinqIntoClauseImpl intoClause = getIntoClause();
		if(intoClause != null)
		{
			if(!intoClause.processDeclarations(processor, state, lastParent, place))
			{
				return false;
			}
		}
		return true;
	}
}
