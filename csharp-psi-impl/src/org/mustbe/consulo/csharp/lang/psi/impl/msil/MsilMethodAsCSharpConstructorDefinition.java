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
import org.mustbe.consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilMethodAsCSharpConstructorDefinition extends MsilMethodAsCSharpLikeMethodDeclaration implements CSharpConstructorDeclaration
{
	private final MsilClassAsCSharpTypeDefinition myTypeDefinition;

	public MsilMethodAsCSharpConstructorDefinition(
			DotNetQualifiedElement buildRoot,
			MsilClassAsCSharpTypeDefinition typeDefinition,
			MsilMethodEntry methodEntry)
	{
		super(buildRoot, methodEntry);
		myTypeDefinition = typeDefinition;
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			((CSharpElementVisitor) visitor).visitConstructorDeclaration(this);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		return DotNetGenericParameter.EMPTY_ARRAY;
	}

	@Override
	public int getGenericParametersCount()
	{
		return 0;
	}

	@Override
	public PsiElement getParent()
	{
		return myTypeDefinition;
	}

	@Override
	public String getName()
	{
		return myTypeDefinition.getName();
	}

	@Override
	public boolean isDeConstructor()
	{
		return false;
	}
}
