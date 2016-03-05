/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.handlers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpNamedResolveSelector;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
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
