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
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.09.14
 */
public abstract class CSharpLightLikeMethodDeclarationWithImplType<S extends DotNetLikeMethodDeclaration & DotNetVirtualImplementOwner> extends
		CSharpLightLikeMethodDeclaration<S> implements DotNetVirtualImplementOwner
{
	private DotNetTypeRef myReturnTypeRef = DotNetTypeRef.ERROR_TYPE;
	private DotNetTypeRef myTypeRefForImplement = DotNetTypeRef.ERROR_TYPE;

	public CSharpLightLikeMethodDeclarationWithImplType(@NotNull S declaration)
	{
		this(declaration, declaration.getParameterList());
	}

	public CSharpLightLikeMethodDeclarationWithImplType(S original, @Nullable DotNetParameterList parameterList)
	{
		super(original, parameterList);
		myReturnTypeRef = original.getReturnTypeRef();
		myTypeRefForImplement = original.getTypeRefForImplement();
	}

	@NotNull
	public CSharpLightLikeMethodDeclarationWithImplType<S> withTypeRefForImplement(@NotNull DotNetTypeRef type)
	{
		myTypeRefForImplement = type;
		return this;
	}

	@NotNull
	public CSharpLightLikeMethodDeclarationWithImplType<S> withReturnTypeRef(@NotNull DotNetTypeRef type)
	{
		myReturnTypeRef = type;
		return this;
	}

	@NotNull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return myTypeRefForImplement;
	}

	@NotNull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return myReturnTypeRef;
	}
}
