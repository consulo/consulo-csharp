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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Function;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
@Deprecated
public abstract class AbstractScopeProcessor extends StubScopeProcessor
{
	protected final List<ResolveResult> myElements = new ArrayList<ResolveResult>();
	private Comparator<ResolveResult> myComparator;

	public void add(ResolveResult resolveResult)
	{
		myElements.add(resolveResult);
	}

	public void addElement(@NotNull PsiElement element)
	{
		if(element instanceof DotNetNamespaceDeclaration)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			PsiElementResolveResult e = new PsiElementResolveResult(element, true);

			myElements.add(e);
		}
	}

	public void merge(@NotNull AbstractScopeProcessor processor)
	{
		myElements.addAll(processor.myElements);
	}

	public boolean isEmpty()
	{
		return myElements.isEmpty();
	}

	@NotNull
	public ResolveResult[] toResolveResults()
	{
		if(myElements.isEmpty())
		{
			return ResolveResult.EMPTY_ARRAY;
		}

		ResolveResult[] resolveResults = ContainerUtil.toArray(myElements, ResolveResult.EMPTY_ARRAY);
		if(myComparator != null)
		{
			Arrays.sort(resolveResults, myComparator);
		}
		return resolveResults;
	}

	@NotNull
	public PsiElement[] toPsiElements()
	{
		final ResolveResult[] resultWithWeights = toResolveResults();
		return ContainerUtil.map(resultWithWeights, new Function<ResolveResult, PsiElement>()
		{
			@Override
			public PsiElement fun(ResolveResult resolveResultWithWeight)
			{
				return resolveResultWithWeight.getElement();
			}
		}, PsiElement.EMPTY_ARRAY);
	}

	public void setComparator(@Nullable Comparator<ResolveResult> comparator)
	{
		myComparator = comparator;
	}
}
