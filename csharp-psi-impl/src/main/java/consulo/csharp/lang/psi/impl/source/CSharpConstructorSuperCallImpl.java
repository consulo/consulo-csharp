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
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetExpression;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpConstructorSuperCallImpl extends CSharpElementImpl implements CSharpConstructorSuperCall
{
	public CSharpConstructorSuperCallImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConstructorSuperCall(this);
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return findChildByClass(CSharpCallArgumentList.class);
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return getExpression().resolve();
	}

	@Override
	@Nonnull
	public CSharpReferenceExpression getExpression()
	{
		return findNotNullChildByClass(CSharpReferenceExpression.class);
	}

	@Nonnull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return getExpression().multiResolve(incompleteCode);
	}

	@Nonnull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? DotNetExpression.EMPTY_ARRAY : parameterList.getExpressions();
	}

	@Nonnull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		CSharpCallArgumentList parameterList = getParameterList();
		return parameterList == null ? CSharpCallArgument.EMPTY_ARRAY : parameterList.getArguments();
	}
}
