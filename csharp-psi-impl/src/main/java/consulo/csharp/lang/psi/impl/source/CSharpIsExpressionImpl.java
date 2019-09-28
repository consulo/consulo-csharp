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

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpIsExpressionImpl extends CSharpExpressionImpl implements DotNetExpression
{
	public CSharpIsExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetTypeRef getIsTypeRef()
	{
		DotNetType type = findChildByClass(DotNetType.class);
		return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
	}

	@Nonnull
	public DotNetExpression getExpression()
	{
		return findNotNullChildByClass(DotNetExpression.class);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitIsExpression(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return new CSharpTypeRefByQName(this, DotNetTypes.System.Boolean);
	}

	@Nullable
	public CSharpIsVariableImpl getVariable()
	{
		return findChildByClass(CSharpIsVariableImpl.class);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		CSharpIsVariableImpl variable = getVariable();
		if(variable != null && !processor.execute(variable, state))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}
}
