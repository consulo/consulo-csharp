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

import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpStatementAsStatementOwner;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.psi.DotNetVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.01.14
 */
public class CSharpForStatementImpl extends CSharpElementImpl implements DotNetStatement, CSharpStatementAsStatementOwner
{
	public CSharpForStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nullable
	@Override
	public DotNetStatement getChildStatement()
	{
		return findChildByClass(DotNetStatement.class);
	}

	@Nonnull
	public DotNetVariable[] getVariables()
	{
		return findChildrenByClass(DotNetVariable.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitForStatement(this);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement
			place)
	{
		if(lastParent == null || !PsiTreeUtil.isAncestor(this, lastParent, false))
		{
			return true;
		}

		for(DotNetVariable dotNetVariable : getVariables())
		{
			if(!processor.execute(dotNetVariable, state))
			{
				return false;
			}
		}
		return true;
	}
}
