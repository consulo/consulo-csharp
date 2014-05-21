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
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 17.12.13.
 */
public class MemberResolveScopeProcessor extends AbstractScopeProcessor
{
	private final Condition<PsiNamedElement> myNameCondition;
	private final WeightProcessor<PsiNamedElement> myWeightProcessor;
	private final boolean myNamed;

	public MemberResolveScopeProcessor(Condition<PsiNamedElement> condition, WeightProcessor<PsiNamedElement> weightProcessor, boolean named)
	{
		myNameCondition = condition;
		myWeightProcessor = weightProcessor;
		myNamed = named;

		//noinspection unchecked
		putUserData(CSharpResolveUtil.CONDITION_KEY, (Condition) condition);
	}

	@Override
	public boolean execute(@NotNull PsiElement element, ResolveState state)
	{
		if(element instanceof DotNetNamedElement)
		{
			if(myNameCondition.value((PsiNamedElement) element))
			{
				int weight = myWeightProcessor.getWeight((PsiNamedElement) element);
				addElement(element, weight);
				if(weight == WeightProcessor.MAX_WEIGHT && myNamed)
				{
					return false;
				}
			}
		}
		return true;
	}
}
