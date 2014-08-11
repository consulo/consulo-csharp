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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpLightMethodDeclarationBuilder extends CSharpLightLikeMethodDeclarationBuilder<CSharpLightMethodDeclarationBuilder> implements
		CSharpMethodDeclaration
{
	private IElementType myOperatorElementType;
	private List<CSharpGenericConstraint> myGenericConstraints = Collections.emptyList();

	public CSharpLightMethodDeclarationBuilder(Project project)
	{
		super(project);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitMethodDeclaration(this);
	}

	@Override
	public boolean isDelegate()
	{
		return false;
	}

	@Override
	public boolean isOperator()
	{
		return myOperatorElementType != null;
	}

	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		return myOperatorElementType;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
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

	@NotNull
	public CSharpLightMethodDeclarationBuilder setOperator(@Nullable IElementType elementType)
	{
		myOperatorElementType = elementType;
		return this;
	}

	@NotNull
	public CSharpLightMethodDeclarationBuilder addGenericConstraint(CSharpGenericConstraint genericConstraint)
	{
		if(myGenericConstraints.isEmpty())
		{
			myGenericConstraints = new ArrayList<CSharpGenericConstraint>(2);
		}
		myGenericConstraints.add(genericConstraint);
		return this;
	}
}
