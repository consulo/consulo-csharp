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
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.01.14
 */
public class CSharpCatchStatementImpl extends CSharpElementImpl implements DotNetStatement
{
	public CSharpCatchStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitCatchStatement(this);
	}

	@RequiredReadAction
	public void deleteVariable()
	{
		CSharpLocalVariable variable = getVariable();
		if(variable == null)
		{
			return;
		}

		PsiElement lparElement = variable.getPrevSibling();
		PsiElement rparElement = variable.getNextSibling();

		((CSharpLocalVariableImpl) variable).deleteInternal();

		if(PsiUtilCore.getElementType(lparElement) == CSharpTokens.LPAR)
		{
			lparElement.delete();
		}

		if(PsiUtilCore.getElementType(rparElement) == CSharpTokens.RPAR)
		{
			rparElement.delete();
		}
	}

	@Nullable
	public CSharpLocalVariable getVariable()
	{
		return findChildByClass(CSharpLocalVariable.class);
	}

	@Nullable
	public DotNetExpression getFilterExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		if(lastParent == null || !PsiTreeUtil.isAncestor(this, lastParent, false))
		{
			return true;
		}
		CSharpLocalVariable variable = getVariable();
		if(variable != null && variable.getNameIdentifier() != null)   // variable can be without name
		{
			if(!processor.execute(variable, state))
			{
				return false;
			}
		}
		return true;
	}
}
