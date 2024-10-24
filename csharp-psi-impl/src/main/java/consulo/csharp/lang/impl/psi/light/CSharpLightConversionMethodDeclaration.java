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

package consulo.csharp.lang.impl.psi.light;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Objects;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class CSharpLightConversionMethodDeclaration extends CSharpLightLikeMethodDeclaration<CSharpConversionMethodDeclaration> implements
		CSharpConversionMethodDeclaration
{
	@Nonnull
	private final DotNetTypeRef myReturnTypeRef;
	@Nonnull
	private final DotNetGenericExtractor myExtractor;

	public CSharpLightConversionMethodDeclaration(CSharpConversionMethodDeclaration original,
												  @Nullable DotNetParameterList parameterList,
												  @Nonnull DotNetTypeRef returnTypeRef,
												  @Nonnull DotNetGenericExtractor extractor)
	{
		super(original, parameterList);
		myExtractor = extractor;
		myOriginal = original;
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

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRef;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder("CSharpLightConversionMethodDeclaration{");
		sb.append("myOriginal=").append(myOriginal);
		sb.append(", myExtractor=").append(myExtractor);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}
		CSharpLightConversionMethodDeclaration that = (CSharpLightConversionMethodDeclaration) o;
		return Objects.equals(myOriginal, that.myOriginal) &&
				Objects.equals(myExtractor, that.myExtractor);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(myOriginal, myExtractor);
	}
}
