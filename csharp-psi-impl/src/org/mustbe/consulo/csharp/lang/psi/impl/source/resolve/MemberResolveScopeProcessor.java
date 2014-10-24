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

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveSelector;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.ResolveState;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
public class MemberResolveScopeProcessor extends AbstractScopeProcessor
{
	private final GlobalSearchScope myScope;

	public MemberResolveScopeProcessor(GlobalSearchScope scope, ResolveResult[] elements, ExecuteTarget[] targets)
	{
		Collections.addAll(myElements, elements);
		myScope = scope;
		putUserData(ExecuteTargetUtil.EXECUTE_TARGETS, ExecuteTargetUtil.of(targets));
	}

	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		CSharpResolveSelector selector = state.get(CSharpResolveUtil.SELECTOR);
		if(selector == null)
		{
			return true;
		}

		DotNetGenericExtractor extractor = state.get(CSharpResolveUtil.EXTRACTOR);
		assert extractor != null;

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(extractor, myScope, element);


		PsiElement psiElement = selector.doSelectElement(context);
		if(psiElement != null)
		{
			PsiElement normalize = normalize(psiElement);
			if(!ExecuteTargetUtil.isMyElement(this, normalize))
			{
				return true;
			}

			addElement(normalize);
			return false;
		}
		return true;
	}

	private static PsiElement normalize(PsiElement element)
	{
		if(element instanceof CSharpElementGroup)
		{
			Collection<? extends PsiElement> elements = ((CSharpElementGroup) element).getElements();
			if(elements.size() == 1)
			{
				PsiElement firstItem = ContainerUtil.getFirstItem(elements);
				if(firstItem instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) firstItem).isDelegate() || !(firstItem instanceof
						DotNetLikeMethodDeclaration))
				{
					return firstItem;
				}
			}
		}
		return element;
	}
}
