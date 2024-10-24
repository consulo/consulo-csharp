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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.SimpleNamedScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.ResolveResult;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.resolve.ResolveState;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class ParameterFromParentKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
			@Nonnull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@Nonnull Processor<ResolveResult> processor)
	{
		PsiElement element = options.getElement();

		DotNetParameterListOwner parameterListOwner = CSharpReferenceExpressionImplUtil.findParentOrNextIfDoc(element, DotNetParameterListOwner
				.class);
		if(parameterListOwner == null)
		{
			return;
		}

		SimpleNamedScopeProcessor scopeProcessor = new SimpleNamedScopeProcessor(processor, options.isCompletion(),
				ExecuteTarget.LOCAL_VARIABLE_OR_PARAMETER_OR_LOCAL_METHOD);
		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.SELECTOR, options.getSelector());

		parameterListOwner.processDeclarations(scopeProcessor, state, null, element);
	}
}
