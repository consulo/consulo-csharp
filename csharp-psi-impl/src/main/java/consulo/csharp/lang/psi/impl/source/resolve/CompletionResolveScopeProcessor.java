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

package consulo.csharp.lang.psi.impl.source.resolve;

import javax.annotation.Nonnull;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpContextUtil;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.csharp.lang.psi.impl.CSharpVisibilityUtil;
import consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 23.10.14
 */
public class CompletionResolveScopeProcessor extends StubScopeProcessor
{
	@Nonnull
	private final GlobalSearchScope myScope;
	@Nonnull
	private final PsiElement myPlace;
	@Nonnull
	private CSharpContextUtil.ContextType myContextType;
	@Nonnull
	private Processor<ResolveResult> myProcessor;

	@RequiredReadAction
	public CompletionResolveScopeProcessor(@Nonnull CSharpResolveOptions options, @Nonnull Processor<ResolveResult> processor, @Nonnull ExecuteTarget[] targets)
	{
		myProcessor = processor;
		myPlace = options.getElement();

		myScope = myPlace.getResolveScope();
		CSharpContextUtil.ContextType completionContextType = options.getCompletionContextType();
		if(completionContextType != null)
		{
			myContextType = completionContextType;
		}
		else
		{
			myContextType = myPlace instanceof CSharpReferenceExpression ? CSharpContextUtil.getParentContextTypeForReference((CSharpReferenceExpression) myPlace) : CSharpContextUtil.ContextType.ANY;
		}
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@Override
	public void pushResultExternally(@Nonnull ResolveResult resolveResult)
	{
		myProcessor.process(resolveResult);
	}

	@Override
	@RequiredReadAction
	public boolean execute(@Nonnull PsiElement element, ResolveState state)
	{
		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		for(PsiElement psiElement : OverrideUtil.getAllMembers(element, myScope, extractor, true, false))
		{
			ProgressManager.checkCanceled();

			if(!ExecuteTargetUtil.isMyElement(this, psiElement))
			{
				continue;
			}

			processElement(psiElement);
		}
		return true;
	}

	@RequiredReadAction
	private void processElement(@Nonnull PsiElement element)
	{
		if(element instanceof DotNetModifierListOwner && !CSharpVisibilityUtil.isVisible((DotNetModifierListOwner) element, myPlace))
		{
			return;
		}

		if(myContextType != CSharpContextUtil.ContextType.ANY)
		{
			CSharpContextUtil.ContextType contextForResolved = CSharpContextUtil.getContextForResolved(element);
			switch(myContextType)
			{
				case INSTANCE:
					if(contextForResolved == CSharpContextUtil.ContextType.STATIC)
					{
						return;
					}
					break;
				case STATIC:
					if(contextForResolved.isAllowInstance())
					{
						return;
					}
					break;
			}
		}
		myProcessor.process(new CSharpResolveResult(element));
	}
}
