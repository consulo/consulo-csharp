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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 22.10.14
 */
public class StubElementResolveResult extends CSharpResolveResult
{
	private final DotNetTypeRef myTypeRef;

	public StubElementResolveResult(@Nonnull PsiElement element, boolean validResult, @Nonnull DotNetTypeRef typeRef)
	{
		super(element, validResult);
		myTypeRef = typeRef;
	}

	@Nonnull
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}
}
