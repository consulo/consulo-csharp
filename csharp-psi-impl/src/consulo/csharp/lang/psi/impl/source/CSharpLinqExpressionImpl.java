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
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLinqExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpLinqExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
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
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLinqExpression(this);
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
			@NotNull ResolveState state,
			PsiElement lastParent,
			@NotNull PsiElement place)
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

	@NotNull
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
