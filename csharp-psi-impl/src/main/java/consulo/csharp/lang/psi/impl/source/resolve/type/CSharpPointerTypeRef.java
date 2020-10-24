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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.*;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 07.12.14
 */
public class CSharpPointerTypeRef extends DotNetTypeRefWithCachedResult implements DotNetPointerTypeRef
{
	private final DotNetTypeRef myInnerTypeRef;

	public CSharpPointerTypeRef(@Nonnull DotNetTypeRef innerTypeRef)
	{
		super(innerTypeRef.getProject(), innerTypeRef.getResolveScope());
		myInnerTypeRef = innerTypeRef;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myProject).findType(DotNetTypes.System.Object, myResolveScope);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(type);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return myInnerTypeRef.toString() + "*";
	}

	@Nonnull
	@Override
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerTypeRef;
	}
}
