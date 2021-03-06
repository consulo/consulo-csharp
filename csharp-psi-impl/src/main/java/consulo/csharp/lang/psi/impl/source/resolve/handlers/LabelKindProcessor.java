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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.psi.impl.source.resolve.ExecuteTarget;
import consulo.csharp.lang.psi.impl.source.resolve.SimpleNamedScopeProcessor;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class LabelKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
			@Nonnull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@Nonnull Processor<ResolveResult> processor)
	{
		PsiElement element = options.getElement();

		SimpleNamedScopeProcessor scopeProcessor = new SimpleNamedScopeProcessor(processor, options.isCompletion(), ExecuteTarget.LABEL);

		DotNetQualifiedElement parentOfType = PsiTreeUtil.getParentOfType(element, DotNetQualifiedElement.class);
		assert parentOfType != null;

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.SELECTOR, options.getSelector());
		CSharpResolveUtil.walkForLabel(scopeProcessor, parentOfType, state);
	}
}
