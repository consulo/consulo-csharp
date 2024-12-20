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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.msil.CSharpTransform;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.*;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CSharpNullTypeRef extends DotNetTypeRefWithCachedResult
{
	@RequiredReadAction
	public CSharpNullTypeRef(@Nonnull Project project, @Nonnull GlobalSearchScope scope)
	{
		super(project, scope);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(getProject()).findType(DotNetTypes.System.Object, myResolveScope, CSharpTransform.INSTANCE);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(type, DotNetGenericExtractor.EMPTY);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getVmQName()
	{
		return "null";
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof CSharpNullTypeRef;
	}

	@Override
	public int hashCode()
	{
		return 1;
	}
}
