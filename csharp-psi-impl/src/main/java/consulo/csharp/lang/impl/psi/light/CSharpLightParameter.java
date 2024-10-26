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
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpLightParameter extends CSharpLightVariable<DotNetParameter> implements DotNetParameter
{
	private Supplier<DotNetTypeRef> myTypeRef;

	public CSharpLightParameter(DotNetParameter original)
	{
		this(original, () -> original.toTypeRef(false));
	}

	public CSharpLightParameter(DotNetParameter original, Supplier<DotNetTypeRef> dotNetTypeRef)
	{
		super(original);
		myTypeRef = dotNetTypeRef;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef(boolean resolveFromInitializer)
	{
		return myTypeRef.get();
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitParameter(this);
	}

	@Nullable
	@Override
	public DotNetParameterListOwner getOwner()
	{
		return myOriginal.getOwner();
	}

	@Override
	public int getIndex()
	{
		return myOriginal.getIndex();
	}
}
