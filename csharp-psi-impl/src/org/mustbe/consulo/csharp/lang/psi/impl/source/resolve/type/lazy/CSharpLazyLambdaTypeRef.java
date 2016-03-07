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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 09.11.14
 */
public class CSharpLazyLambdaTypeRef extends CSharpLambdaTypeRef
{
	@NotNull
	private final PsiElement myScope;

	public CSharpLazyLambdaTypeRef(@NotNull PsiElement scope, @NotNull CSharpMethodDeclaration method)
	{
		super(method);
		myScope = scope;
	}

	public CSharpLazyLambdaTypeRef(@NotNull PsiElement scope,
			@Nullable CSharpMethodDeclaration target,
			@NotNull CSharpSimpleParameterInfo[] parameterInfos,
			@NotNull DotNetTypeRef returnType)
	{
		super(target, parameterInfos, returnType);
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
	public DotNetTypeResolveResult resolveImpl()
	{
		return CSharpLazyLambdaTypeRef.super.resolve(myScope);
	}
}