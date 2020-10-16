/*
 * Copyright 2013-2019 consulo.io
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
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetVariable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2019-10-05
 */
public class CSharpCasePatternStatementImpl extends CSharpElementImpl implements CSharpSwitchLabelStatement
{
	public CSharpCasePatternStatementImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetVariable getVariable()
	{
		return findNotNullChildByClass(CSharpCaseVariableImpl.class);
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getWhenExpression()
	{
		return findChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitCasePatternStatement(this);
	}

	@Override
	@RequiredReadAction
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetVariable variable = getVariable();
		if(!processor.execute(variable, state))
		{
			return false;
		}

		DotNetExpression whenExpression = getWhenExpression();
		if(whenExpression != null && !whenExpression.processDeclarations(processor, state, lastParent, place))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
