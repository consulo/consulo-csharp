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

package consulo.csharp.lang.psi.impl.source.resolve.handlers;

import gnu.trove.THashMap;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResultWithExtractor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefFromGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class ThisKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull Processor<ResolveResult> processor)
	{
		DotNetTypeDeclaration thisTypeDeclaration = PsiTreeUtil.getContextOfType(options.getElement(), DotNetTypeDeclaration.class);
		if(thisTypeDeclaration != null)
		{
			DotNetGenericExtractor genericExtractor = DotNetGenericExtractor.EMPTY;
			int genericParametersCount = thisTypeDeclaration.getGenericParametersCount();
			if(genericParametersCount > 0)
			{
				Map<DotNetGenericParameter, DotNetTypeRef> map = new THashMap<DotNetGenericParameter, DotNetTypeRef>(genericParametersCount);
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
