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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpMethodDefinition extends MsilMethodAsCSharpLikeMethodDefinition implements CSharpMethodDeclaration
{
	public MsilMethodAsCSharpMethodDefinition(MsilMethodEntry methodEntry)
	{
		super(methodEntry);
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return new CSharpGenericConstraint[0];
	}

	@Override
	public boolean isDelegate()
	{
		return false;
	}

	@Override
	public boolean isOperator()
	{
		return false;
	}

	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}
}
