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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class ParameterKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@NotNull CSharpResolveOptions options,
			@NotNull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@NotNull Processor<ResolveResult> processor)
	{
		final CSharpCallArgumentListOwner callArgumentListOwner = options.getCallArgumentListOwner();

		CSharpResolveSelector selector = options.getSelector();

		if(callArgumentListOwner == null)
		{
			return;
		}

		ResolveResult[] resolveResults = callArgumentListOwner.multiResolve(false);
		ResolveResult goodResolveResult = CSharpResolveUtil.findValidOrFirstMaybeResult(resolveResults);

		if(goodResolveResult == null)
		{
			return;
		}

		if(!(goodResolveResult instanceof MethodResolveResult))
		{
			return;
		}

		MethodCalcResult ourCalcResult = ((MethodResolveResult) goodResolveResult).getCalcResult();

		for(NCallArgument o : ourCalcResult.getArguments())
		{
			PsiElement parameterElement = o.getParameterElement();
			if(parameterElement != null)
			{
				if(selector != null)
				{
					String parameterName = o.getParameterName();
					assert parameterName != null;
					if(selector instanceof CSharpNamedResolveSelector && ((CSharpNamedResolveSelector) selector).isNameEqual(parameterName))
					{
						processor.process(new CSharpResolveResult(parameterElement));
					}
				}
				else
				{
					processor.process(new CSharpResolveResult(parameterElement));
				}
			}
		}
	}
}
