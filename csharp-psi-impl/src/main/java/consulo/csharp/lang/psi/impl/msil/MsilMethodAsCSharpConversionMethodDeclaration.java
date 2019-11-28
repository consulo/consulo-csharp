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

package consulo.csharp.lang.psi.impl.msil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilMethodEntry;

/**
 * @author VISTALL
 * @since 13.06.14
 */
public class MsilMethodAsCSharpConversionMethodDeclaration extends MsilMethodAsCSharpLikeMethodDeclaration implements CSharpConversionMethodDeclaration
{
	private final NotNullLazyValue<DotNetTypeRef> myReturnTypeRefValue;

	public MsilMethodAsCSharpConversionMethodDeclaration(PsiElement parent, MsilMethodEntry methodEntry)
	{
		super(parent, methodEntry);
		myReturnTypeRefValue = NotNullLazyValue.createValue(() -> MsilToCSharpUtil.extractToCSharp(myOriginal.getReturnTypeRef(), myOriginal));
	}

	@Override
	public String getName()
	{
		return isImplicit() ? "<implicit>" : "<explicit>";
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConversionMethodDeclaration(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRefValue.getValue();
	}

	@Override
	public boolean isImplicit()
	{
		if(Comparing.equal(myOriginal.getName(), "op_Explicit"))
		{
			return false;
		}
		else if(Comparing.equal(myOriginal.getName(), "op_Implicit"))
		{
			return true;
		}
		else
		{
			throw new IllegalArgumentException(myOriginal.getName());
		}
	}

	@Nonnull
	@Override
	public DotNetTypeRef getConversionTypeRef()
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

	@Nullable
	@Override
	public DotNetType getConversionType()
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getOperatorElement()
	{
		return null;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpConversionMethodDeclaration.class;
	}
}
