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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 13.08.14
 */
public abstract class CSharpExpressionWithOperatorImpl extends CSharpElementImpl implements DotNetExpression, CSharpCallArgumentListOwner
{
	public CSharpExpressionWithOperatorImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	public CSharpOperatorReferenceImpl getOperatorElement()
	{
		return findNotNullChildByClass(CSharpOperatorReferenceImpl.class);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromParent)
	{
		CSharpOperatorReferenceImpl operatorElement = getOperatorElement();

		return operatorElement.resolveToTypeRef();
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

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		return findChildrenByClass(DotNetExpression.class);
	}

	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return getOperatorElement().getTypeArgumentList();
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		return getOperatorElement().getTypeArgumentListRefs();
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return getOperatorElement().resolve();
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return getOperatorElement().multiResolve(incompleteCode);
	}
}
