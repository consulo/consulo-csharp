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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.lazy;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericWrapperTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 04.01.14.
 */
public class CSharpLazyGenericWrapperTypeRef extends CSharpGenericWrapperTypeRef
{
	@NotNull
	private final PsiElement myScope;

	public CSharpLazyGenericWrapperTypeRef(@NotNull PsiElement scope, @NotNull DotNetTypeRef innerTypeRef, @NotNull DotNetTypeRef[] rArguments)
	{
		super(innerTypeRef, rArguments);
		myScope = scope;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeResolveResult resolve(@NotNull PsiElement scope)
	{
		return resolveImpl();
	}

	@NotNull
	@LazyInstance
	@RequiredReadAction
	private DotNetTypeResolveResult resolveImpl()
	{
		return CSharpLazyGenericWrapperTypeRef.super.resolve(myScope);
	}
}
