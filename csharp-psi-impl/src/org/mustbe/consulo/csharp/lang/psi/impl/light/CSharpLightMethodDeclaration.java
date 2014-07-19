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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 13.01.14
 */
public class CSharpLightMethodDeclaration extends CSharpLightLikeMethodDeclaration<CSharpMethodDeclaration> implements CSharpMethodDeclaration
{
	public CSharpLightMethodDeclaration(CSharpMethodDeclaration original, DotNetTypeRef returnTypeRef, @NotNull DotNetParameterList parameterList)
	{
		super(original, returnTypeRef, parameterList);
	}

	@Override
	public boolean isDelegate()
	{
		return myOriginal.isDelegate();
	}

	@Override
	public boolean isOperator()
	{
		return myOriginal.isOperator();
	}

	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		return myOriginal.getOperatorElementType();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return myOriginal.getNameIdentifier();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitMethodDeclaration(this);
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
		return CSharpGenericConstraint.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}
}
