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

package consulo.csharp.lang.psi.impl.light;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class CSharpLightConversionMethodDeclaration extends CSharpLightLikeMethodDeclaration<CSharpConversionMethodDeclaration> implements
		CSharpConversionMethodDeclaration
{
	@Nonnull
	private final DotNetTypeRef myReturnTypeRef;

	public CSharpLightConversionMethodDeclaration(CSharpConversionMethodDeclaration original,
			@Nullable DotNetParameterList parameterList,
			@Nonnull DotNetTypeRef returnTypeRef)
	{
		super(original, parameterList);
		myReturnTypeRef = returnTypeRef;
	}

	@Override
	public boolean isImplicit()
	{
		return myOriginal.isImplicit();
	}

	@Nonnull
	@Override
	public DotNetTypeRef getConversionTypeRef()
	{
		return myOriginal.getConversionTypeRef();
	}

	@Nullable
	@Override
	public DotNetType getConversionType()
	{
		return myOriginal.getConversionType();
	}

	@Nullable
	@Override
	public PsiElement getOperatorElement()
	{
		return null;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitConversionMethodDeclaration(this);
	}

	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRef;
	}
}
