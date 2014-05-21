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

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;

/**
 * @author VISTALL
 * @since 05.05.14
 */
@ArrayFactoryFields
public class ResolveResultWithWeight extends PsiElementResolveResult
{
	private final int myWeight;

	public ResolveResultWithWeight(@NotNull PsiElement element)
	{
		this(element, WeightProcessor.MAX_WEIGHT);
	}

	public ResolveResultWithWeight(@NotNull PsiElement element, int weight)
	{
		super(element, element.isValid());
		myWeight = weight;
	}

	public int getWeight()
	{
		return myWeight;
	}

	public boolean isGoodResult()
	{
		return myWeight == WeightProcessor.MAX_WEIGHT;
	}

	@Override
	public boolean equals(Object o)
	{
		return o instanceof ResolveResultWithWeight && Comparing.equal(getElement(), ((ResolveResultWithWeight) o).getElement());
	}
}
