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

import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.09.14
 */
public abstract class CSharpLightLikeMethodDeclarationWithImplType<S extends DotNetLikeMethodDeclaration & DotNetVirtualImplementOwner> extends
		CSharpLightLikeMethodDeclaration<S> implements DotNetVirtualImplementOwner
{
	private DotNetTypeRef myReturnTypeRef = DotNetTypeRef.ERROR_TYPE;
	private DotNetTypeRef myTypeRefForImplement = DotNetTypeRef.ERROR_TYPE;

	public CSharpLightLikeMethodDeclarationWithImplType(@Nonnull S declaration)
	{
		this(declaration, declaration.getParameterList());
	}

	public CSharpLightLikeMethodDeclarationWithImplType(S original, @Nullable DotNetParameterList parameterList)
	{
		super(original, parameterList);
		myReturnTypeRef = original.getReturnTypeRef();
		myTypeRefForImplement = original.getTypeRefForImplement();
	}

	@Nonnull
	public CSharpLightLikeMethodDeclarationWithImplType<S> withTypeRefForImplement(@Nonnull DotNetTypeRef type)
	{
		myTypeRefForImplement = type;
		return this;
	}

	@Nonnull
	public CSharpLightLikeMethodDeclarationWithImplType<S> withReturnTypeRef(@Nonnull DotNetTypeRef type)
	{
		myReturnTypeRef = type;
		return this;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return myTypeRefForImplement;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRef;
	}
}
