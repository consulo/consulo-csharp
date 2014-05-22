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
import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilWrapperScopeProcessor;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
public abstract class AbstractScopeProcessor extends MsilWrapperScopeProcessor implements PsiScopeProcessor
{
	private static final Comparator<ResolveResultWithWeight> ourWeightComparator = new Comparator<ResolveResultWithWeight>()
	{
		@Override
		public int compare(ResolveResultWithWeight o1, ResolveResultWithWeight o2)
		{
			return o2.getWeight() - o1.getWeight();
		}
	};
	protected final List<ResolveResultWithWeight> myElements = new ArrayList<ResolveResultWithWeight>();

	public void add(ResolveResultWithWeight resolveResult)
	{
		myElements.add(resolveResult);
	}

	public void addElement(PsiElement element, int weight)
	{
		if(element instanceof DotNetNamespaceDeclaration)
		{
			throw new IllegalArgumentException();
		}
		else
		{
			ResolveResultWithWeight e = new ResolveResultWithWeight(element, weight);
			if(myElements.contains(e))
			{
				return;
			}
			myElements.add(e);
		}
	}

	public void merge(AbstractScopeProcessor processor)
	{
		myElements.addAll(processor.myElements);
	}

	public boolean isEmpty()
	{
		return myElements.isEmpty();
	}

	@NotNull
	public ResolveResultWithWeight[] toResolveResults()
	{
		if(myElements.isEmpty())
		{
			return ResolveResultWithWeight.EMPTY_ARRAY;
		}

		ResolveResultWithWeight[] resultWithWeights = ContainerUtil.toArray(myElements, ResolveResultWithWeight.ARRAY_FACTORY);
		ContainerUtil.sort(resultWithWeights, ourWeightComparator);
		return resultWithWeights;
	}
}
