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
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-09-25
 */
public class CSharpOutRefVariableExpressionImpl extends CSharpExpressionImpl implements CSharpOutRefExpression
{
	public CSharpOutRefVariableExpressionImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	public DotNetVariable getVariable()
	{
		return findNotNullChildByClass(CSharpOutRefVariableImpl.class);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return getVariable().toTypeRef(resolveFromParent);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitOutRefVariableExpression(this);
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		DotNetVariable variable = getVariable();
		if(!processor.execute(variable, state))
		{
			return false;
		}
		return super.processDeclarations(processor, state, lastParent, place);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpRefTypeRef.Type getExpressionType()
	{
		PsiElement element = findNotNullChildByFilter(CSharpOutRefWrapExpressionImpl.ourStartTypes);
		IElementType type = PsiUtilCore.getElementType(element);
		if(type == CSharpTokens.REF_KEYWORD)
		{
			return CSharpRefTypeRef.Type.ref;
		}
		else
		{
			return CSharpRefTypeRef.Type.out;
		}
	}
}
