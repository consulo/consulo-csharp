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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.partial.CSharpCompositeTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpElementGroupImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.overrideSystem.OverrideUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpGenericExtractor;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
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

	public MemberResolveScopeProcessor(@NotNull CSharpResolveOptions options, @NotNull Processor<ResolveResult> resultProcessor, ExecuteTarget[] targets)
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
		applyTypeArguments(psiElements);
		psiElements = CSharpCompositeTypeDeclaration.wrapPartialTypes(myResolveScope, myScopeElement.getProject(), psiElements);

		for(PsiElement psiElement : OverrideUtil.filterOverrideElements(this, myScopeElement, psiElements, myOverrideProcessor))
		{
			ProgressManager.checkCanceled();

			if(!ExecuteTargetUtil.isMyElement(this, psiElement))
			{
				continue;
			}

			final CSharpResolveResult result = new CSharpResolveResult(psiElement);
			result.setProvider(element);
			result.setAssignable(myScopeElement);

			if(!myResultProcessor.process(result))
			{
				return false;
			}
		}
		return true;
	}

	@RequiredReadAction
	private void applyTypeArguments(PsiElement[] psiElements)
	{
		if(!(myScopeElement instanceof CSharpReferenceExpression) || psiElements.length == 0)
		{
			return;
		}

		int typeArgumentListSize = CSharpReferenceExpressionImplUtil.getTypeArgumentListSize(myScopeElement);
		if(typeArgumentListSize == 0)
		{
			return;
		}

		DotNetTypeRef[] typeArgumentListRefs = ((CSharpReferenceExpression) myScopeElement).getTypeArgumentListRefs();

		for(int i = 0; i < psiElements.length; i++)
		{
			PsiElement psiElement = psiElements[i];
			if(psiElement instanceof CSharpElementGroup)
			{
				@SuppressWarnings("unchecked") CSharpElementGroup<PsiElement> elementGroup = (CSharpElementGroup<PsiElement>) psiElement;

				Collection<PsiElement> elements = elementGroup.getElements();

				boolean changed = false;

				final List<PsiElement> anotherItems = new ArrayList<PsiElement>(elements.size());
				for(PsiElement temp : elements)
				{
					if(temp instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) temp).getGenericParametersCount() == typeArgumentListSize)
					{
						DotNetGenericParameter[] genericParameters = ((CSharpMethodDeclaration) temp).getGenericParameters();

						DotNetGenericExtractor extractor = CSharpGenericExtractor.create(genericParameters, typeArgumentListRefs);

						anotherItems.add(GenericUnwrapTool.extract((CSharpMethodDeclaration) temp, extractor));
						changed = true;
					}
					else
					{
						anotherItems.add(temp);
					}
				}

				if(changed)
				{
					psiElements[i] = new CSharpElementGroupImpl<PsiElement>(elementGroup.getProject(), elementGroup.getKey(), anotherItems);
				}
			}
		}
	}
}
