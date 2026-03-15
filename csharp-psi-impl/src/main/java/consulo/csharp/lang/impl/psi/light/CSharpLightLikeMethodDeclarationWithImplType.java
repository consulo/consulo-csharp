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
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.resolve.DotNetTypeRef;

import org.jspecify.annotations.Nullable;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 06.09.14
 */
public abstract class CSharpLightLikeMethodDeclarationWithImplType<S extends DotNetLikeMethodDeclaration & DotNetVirtualImplementOwner> extends
		CSharpLightLikeMethodDeclaration<S> implements DotNetVirtualImplementOwner
{
	@Nullable
	private Supplier<DotNetTypeRef> myReturnTypeRef;
	@Nullable
	private Supplier<DotNetTypeRef> myTypeRefForImplement;

	@RequiredReadAction
	public CSharpLightLikeMethodDeclarationWithImplType(S declaration)
	{
		this(declaration, declaration.getParameterList());
	}

	@RequiredReadAction
	public CSharpLightLikeMethodDeclarationWithImplType(S original, @Nullable DotNetParameterList parameterList)
	{
		super(original, parameterList);
	}

	public CSharpLightLikeMethodDeclarationWithImplType<S> withTypeRefForImplement(Supplier<DotNetTypeRef> typeRef)
	{
		if(myTypeRefForImplement != null)
		{
			throw new UnsupportedOperationException();
		}

		myTypeRefForImplement = typeRef;
		return this;
	}

	public CSharpLightLikeMethodDeclarationWithImplType<S> withReturnTypeRef(Supplier<DotNetTypeRef> typeRef)
	{
		if (myReturnTypeRef != null)
		{
			throw new UnsupportedOperationException();
		}

		myReturnTypeRef = typeRef;
		return this;
	}

	public CSharpLightLikeMethodDeclarationWithImplType<S> withTypeRefForImplement(DotNetTypeRef type)
	{
		return withTypeRefForImplement(() -> type);
	}

	public CSharpLightLikeMethodDeclarationWithImplType<S> withReturnTypeRef(DotNetTypeRef type)
	{
		return withReturnTypeRef(() -> type);
	}

	@Override
	@SuppressWarnings("unchecked")
	public DotNetTypeRef getTypeRefForImplement()
	{
		if(myTypeRefForImplement == null)
		{
			return ((S) getOriginalElement()).getTypeRefForImplement();
		}

		return myTypeRefForImplement.get();
	}

	@RequiredReadAction
	@Override
	@SuppressWarnings("unchecked")
	public DotNetTypeRef getReturnTypeRef()
	{
		if(myReturnTypeRef == null)
		{
			return ((S) getOriginalElement()).getReturnTypeRef();
		}

		return myReturnTypeRef.get();
	}
}
