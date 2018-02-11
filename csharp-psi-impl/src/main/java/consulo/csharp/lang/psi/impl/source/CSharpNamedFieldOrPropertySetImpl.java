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

import org.jetbrains.annotations.NonNls;

import javax.annotation.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpNamedFieldOrPropertySet;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpNamedFieldOrPropertySetImpl extends CSharpElementImpl implements CSharpNamedFieldOrPropertySet
{
	public CSharpNamedFieldOrPropertySetImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Nullable
	@Override
	public String getName()
	{
		CSharpReferenceExpression nameReferenceExpression = getNameElement();
		return nameReferenceExpression.getReferenceName();
	}

	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitNamedFieldOrPropertySet(this);
	}

	@Nonnull
	@Override
	public CSharpReferenceExpression getNameElement()
	{
		return (CSharpReferenceExpression) getFirstChild();
	}

	@Nullable
	@Override
	public DotNetExpression getValueExpression()
	{
		PsiElement lastChild = getLastChild();
		if(lastChild instanceof DotNetExpression && lastChild != getNameElement())
		{
			return (DotNetExpression) lastChild;
		}
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return getNameElement();
	}
}
