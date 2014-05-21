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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpTypeDefTypeRef extends DotNetTypeRef.Adapter
{
	private final String myQualifiedName;
	private final int myGenericCount;

	public CSharpTypeDefTypeRef(String qualifiedName, int genericCount)
	{
		myQualifiedName = qualifiedName;
		myGenericCount = genericCount;
	}

	@Nullable
	@Override
	public String getPresentableText()
	{
		return StringUtil.getShortName(myQualifiedName);
	}

	@Nullable
	@Override
	public String getQualifiedText()
	{
		return myQualifiedName;
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		return DotNetPsiFacade.getInstance(scope.getProject()).findType(myQualifiedName, scope.getResolveScope(), myGenericCount);
	}
}
