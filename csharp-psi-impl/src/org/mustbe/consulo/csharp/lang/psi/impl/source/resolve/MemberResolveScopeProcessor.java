/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
public class MemberResolveScopeProcessor extends StubScopeProcessor
{
	private final PsiElement myScopeElement;
	protected Processor<ResolveResult> myResultProcessor;
	private final GlobalSearchScope myResolveScope;
	private final OverrideProcessor myOverrideProcessor;

	public MemberResolveScopeProcessor(@NotNull CSharpResolveOptions options,
			@NotNull Processor<ResolveResult> resultProcessor,
			ExecuteTarget[] targets)
	{
		myScopeElement = options.getElement();
		myResolveScope = myScopeElement.getResolveScope();
		myResultProcessor = resultProcessor;
		myOverrideProcessor = OverrideProcessor.ALWAYS_TRUE;
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	public MemberResolveScopeProcessor(@NotNull PsiElement scopeElement,
			@NotNull Processor<ResolveResult> resultProcessor,
			@Nullable ExecuteTarget[] targets,
			@Nullable OverrideProcessor overrideProcessor)
	{
		myScopeElement = scopeElement;
		myResultProcessor = resultProcessor;
		myResolveScope = scopeElement.getResolveScope();
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
		myOverrideProcessor = overrideProcessor;
	}

	@Override
	public void pushResultExternally(@NotNull ResolveResult resolveResult)
	{
		myResultProcessor.process(resolveResult);
	}

	@RequiredReadAction
	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
		if(selector == null)
		{
			return true;
		}

		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myResolveScope, element);

		PsiElement[] psiElements = selector.doSelectElement(context, state.get(CSharpResolveUtil.WALK_DEEP) == Boolean.TRUE);
		psiElements = CSharpCompositeTypeDeclaration.wrapPartialTypes(myResolveScope, myScopeElement.getProject(), psiElements);

		for(PsiElement psiElement : OverrideUtil.filterOverrideElements(this, myScopeElement, psiElements, myOverrideProcessor))
		{
			ProgressManager.checkCanceled();

			if(!ExecuteTargetUtil.isMyElement(this, psiElement))
			{
				continue;
			}

			if(!myResultProcessor.process(new PsiElementResolveResult(psiElement, true)))
			{
				return false;
			}
		}
		return true;
	}
}
