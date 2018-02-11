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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpElementVisitor;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpAssignmentExpressionImpl extends CSharpExpressionWithOperatorImpl implements DotNetExpression
{
	public CSharpAssignmentExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nonnull
	@RequiredReadAction
	public DotNetExpression getLeftExpression()
	{
		PsiElement firstChild = getFirstChild();
		if(firstChild instanceof DotNetExpression)
		{
			return (DotNetExpression) firstChild;
		}
		throw new IllegalArgumentException();
	}

	@Nullable
	@RequiredReadAction
	public DotNetExpression getRightExpression()
	{
		DotNetExpression[] parameterExpressions = getParameterExpressions();
		if(parameterExpressions.length == 2)
		{
			return parameterExpressions[1];
		}
		return null;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitAssignmentExpression(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromParent)
	{
		return getLeftExpression().toTypeRef(false);
	}
}
