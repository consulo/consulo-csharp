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

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetPointerTypeRef;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 07.12.14
 */
public class CSharpPointerTypeRef extends DotNetTypeRefWithCachedResult implements DotNetPointerTypeRef
{
	private PsiElement myScope;
	private DotNetTypeRef myInnerTypeRef;

	public CSharpPointerTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef innerTypeRef)
	{
		super(scope.getProject());
		myScope = scope;
		myInnerTypeRef = innerTypeRef;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myScope.getProject()).findType(DotNetTypes.System.Object, myScope.getResolveScope());
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(myScope);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myInnerTypeRef.toString() + "*";
	}

	@NotNull
	@Override
	public DotNetTypeRef getInnerTypeRef()
	{
		return myInnerTypeRef;
	}
}
