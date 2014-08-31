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
import org.mustbe.consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.msil.lang.psi.MsilMethodEntry;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

/**
 * @author VISTALL
 * @since 13.06.14
 */
public class MsilMethodAsCSharpConversionMethodDeclaration extends MsilMethodAsCSharpLikeMethodDeclaration implements
		CSharpConversionMethodDeclaration
{
	public MsilMethodAsCSharpConversionMethodDeclaration(PsiElement parent, MsilMethodEntry methodEntry)
	{
		super(parent, methodEntry);
	}

	@Override
	public String getName()
	{
		return super.getName();
	}

	@Override
	public void accept(@NotNull PsiElementVisitor visitor)
	{
		if(visitor instanceof CSharpElementVisitor)
		{
			((CSharpElementVisitor) visitor).visitConversionMethodDeclaration(this);
		}
		else
		{
			visitor.visitElement(this);
		}
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		if(isImplicit())
		{
			return CSharpStaticTypeRef.IMPLICIT;
		}
		else
		{
			return CSharpStaticTypeRef.EXPLICIT;
		}
	}

	@Override
	public boolean isImplicit()
	{
		if(Comparing.equal(myMsilElement.getName(), "op_Explicit"))
		{
			return true;
		}
		else if(Comparing.equal(myMsilElement.getName(), "op_Implicit"))
		{
			return false;
		}
		else
		{
			throw new IllegalArgumentException(myMsilElement.getName());
		}
	}

	@NotNull
	@Override
	public DotNetTypeRef getConversionTypeRef()
	{
		return MsilToCSharpUtil.extractToCSharp(myMsilElement.getReturnTypeRef(), myMsilElement);
	}

	@Nullable
	@Override
	public DotNetType getConversionType()
	{
		return null;
	}
}
