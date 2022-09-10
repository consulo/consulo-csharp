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
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 13.08.14
 */
public abstract class CSharpExpressionWithOperatorImpl extends CSharpExpressionImpl implements DotNetExpression, CSharpCallArgumentListOwner
{
	public CSharpExpressionWithOperatorImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Nonnull
	public CSharpOperatorReferenceImpl getOperatorElement()
	{
		return findNotNullChildByClass(CSharpOperatorReferenceImpl.class);
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		CSharpOperatorReferenceImpl operatorElement = getOperatorElement();

		return operatorElement.resolveToTypeRef();
	}

	@Nonnull
	@Override
	public CSharpCallArgument[] getCallArguments()
	{
		return getOperatorElement().getCallArguments();
	}

	@Override
	public boolean canResolve()
	{
		return getOperatorElement().canResolve();
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return getOperatorElement().getParameterList();
	}

	@Nonnull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		return findChildrenByClass(DotNetExpression.class);
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return getOperatorElement().resolve();
	}

	@Nonnull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return getOperatorElement().multiResolve(incompleteCode);
	}
}
