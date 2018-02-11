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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 04-Nov-17
 */
public class CSharpUndefinedLambdaResolveResult implements CSharpLambdaResolveResult
{
	public static final CSharpUndefinedLambdaResolveResult INSTANCE = new CSharpUndefinedLambdaResolveResult();

	@RequiredReadAction
	@Override
	public boolean isInheritParameters()
	{
		return true;
	}

	@Nullable
	@Override
	public CSharpMethodDeclaration getTarget()
	{
		return null;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public CSharpSimpleParameterInfo[] getParameterInfos()
	{
		return CSharpSimpleParameterInfo.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef getReturnTypeRef()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Nullable
	@Override
	public PsiElement getElement()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetGenericExtractor getGenericExtractor()
	{
		return DotNetGenericExtractor.EMPTY;
	}

	@Override
	public boolean isNullable()
	{
		return false;
	}
}
