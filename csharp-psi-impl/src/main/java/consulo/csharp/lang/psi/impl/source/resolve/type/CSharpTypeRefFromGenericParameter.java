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

import com.intellij.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpTypeRefFromGenericParameter extends DotNetTypeRefWithCachedResult
{
	private final DotNetGenericParameter myGenericParameter;

	public CSharpTypeRefFromGenericParameter(DotNetGenericParameter genericParameter)
	{
		super(genericParameter.getProject());
		myGenericParameter = genericParameter;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return new CSharpUserTypeRef.Result<PsiElement>(myGenericParameter, DotNetGenericExtractor.EMPTY);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return myGenericParameter.getName();
	}
}
