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
import consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetPsiSearcher;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;
import consulo.dotnet.resolve.SimpleTypeResolveResult;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CSharpNullTypeRef extends DotNetTypeRefWithCachedResult
{
	private GlobalSearchScope myScope;

	@RequiredReadAction
	public CSharpNullTypeRef(@NotNull PsiElement element)
	{
		this(element.getProject(), element.getResolveScope());
	}

	@RequiredReadAction
	public CSharpNullTypeRef(@NotNull Project project, @NotNull GlobalSearchScope scope)
	{
		super(project);
		myScope = scope;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(getProject()).findType(DotNetTypes.System.Object, myScope, CSharpTransform.INSTANCE);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		return new SimpleTypeResolveResult(type, DotNetGenericExtractor.EMPTY);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return "null";
	}
}
