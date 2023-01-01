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

import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 28.10.14
 */
public class CSharpResolveResultWithExtractor extends CSharpResolveResult
{
	@Nonnull
	public static CSharpResolveResultWithExtractor withExtractor(@Nonnull ResolveResult resolveResult, @Nonnull DotNetGenericExtractor extractor)
	{
		PsiElement providerElement = null;
		if(resolveResult instanceof CSharpResolveResult)
		{
			providerElement = ((CSharpResolveResult) resolveResult).getProviderElement();
		}
		CSharpResolveResultWithExtractor withExtractor = new CSharpResolveResultWithExtractor(resolveResult.getElement(), extractor);
		withExtractor.setProvider(providerElement);
		return withExtractor;
	}

	@Nonnull
	private final DotNetGenericExtractor myExtractor;

	public CSharpResolveResultWithExtractor(@Nonnull PsiElement element, @Nonnull DotNetGenericExtractor extractor)
	{
		super(element);
		myExtractor = extractor;
	}

	@Nonnull
	public DotNetGenericExtractor getExtractor()
	{
		return myExtractor;
	}
}
