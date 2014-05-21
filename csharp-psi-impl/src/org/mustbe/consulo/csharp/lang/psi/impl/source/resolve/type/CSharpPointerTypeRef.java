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
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.01.14.
 */
public class CSharpPointerTypeRef extends DotNetTypeRef.Adapter implements DotNetPointerTypeRef
{
	private final DotNetTypeRef myInnerType;

	public CSharpPointerTypeRef(DotNetTypeRef innerType)
	{
		myInnerType = innerType;
	}

	@Nullable
	@Override
	public String getPresentableText()
	{
		return myInnerType.getPresentableText() + "*";
	}

	@Nullable
	@Override
	public String getQualifiedText()
	{
		return myInnerType.getQualifiedText() + "*";
	}

	@Override
	public boolean isNullable()
	{
		return myInnerType.isNullable();
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		return myInnerType.resolve(scope);
	}

	@NotNull
	public DotNetTypeRef getInnerType()
	{
		return myInnerType;
	}
}
