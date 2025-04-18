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

package consulo.csharp.lang.impl.psi.source.resolve.handlers;

import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.application.util.function.Processor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveResultWithExtractor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefFromGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.ResolveResult;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class ThisKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
			@Nonnull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@Nonnull Processor<ResolveResult> processor)
	{
		DotNetTypeDeclaration thisTypeDeclaration = PsiTreeUtil.getContextOfType(options.getElement(), DotNetTypeDeclaration.class);
		if(thisTypeDeclaration != null)
		{
			thisTypeDeclaration = CSharpCompositeTypeDeclaration.selectCompositeOrSelfType(thisTypeDeclaration);

			DotNetGenericExtractor genericExtractor = DotNetGenericExtractor.EMPTY;
			int genericParametersCount = thisTypeDeclaration.getGenericParametersCount();
			if(genericParametersCount > 0)
			{
				Map<DotNetGenericParameter, DotNetTypeRef> map = new HashMap<>(genericParametersCount);
				for(DotNetGenericParameter genericParameter : thisTypeDeclaration.getGenericParameters())
				{
					map.put(genericParameter, new CSharpTypeRefFromGenericParameter(genericParameter));
				}
				genericExtractor = CSharpGenericExtractor.create(map);
			}
			processor.process(new CSharpResolveResultWithExtractor(thisTypeDeclaration, genericExtractor));
		}
	}
}
