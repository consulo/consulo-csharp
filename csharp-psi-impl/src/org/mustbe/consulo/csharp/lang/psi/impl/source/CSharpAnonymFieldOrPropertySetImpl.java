/*
 * Copyright 2013-2015 must-be.org
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
import org.mustbe.consulo.csharp.lang.psi.CSharpAnonymFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.04.2015
 */
public class CSharpAnonymFieldOrPropertySetImpl extends CSharpElementImpl implements CSharpAnonymFieldOrPropertySet
{
	public CSharpAnonymFieldOrPropertySetImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Nullable
	@Override
	public String getName()
	{
		DotNetExpression valueExpression = getValueExpression();
		if(valueExpression instanceof CSharpReferenceExpression)
		{
			return  ((CSharpReferenceExpression) valueExpression).getReferenceName();
		}
		return null;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitFieldOrPropertySet(this);
	}

	@NotNull
	@Override
	public PsiElement getNameElement()
	{
		return getFirstChild();
	}

	@NotNull
	@Override
	public DotNetExpression getValueExpression()
	{
		return findNotNullChildByClass(DotNetExpression.class);
	}
}
