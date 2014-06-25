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
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public class OperatorResolveScopeProcessor extends AbstractScopeProcessor
{
	private Condition<DotNetNamedElement> myCond;
	private WeightProcessor<DotNetNamedElement> myWeightProcessor;

	public OperatorResolveScopeProcessor(Condition<DotNetNamedElement> cond, WeightProcessor<DotNetNamedElement> weightProcessor)
	{
		myCond = cond;
		myWeightProcessor = weightProcessor;
		putUserData(CSharpResolveUtil.CONDITION_KEY, new Condition<PsiElement>()
		{
			@Override
			public boolean value(PsiElement element)
			{
				//fixme [vistall] check for operators?
				return element instanceof CSharpTypeDeclaration;
			}
		});
	}

	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		assert element instanceof CSharpTypeDeclaration;

		for(DotNetNamedElement dotNetNamedElement : ((CSharpTypeDeclaration) element).getMembers())
		{
			if(!myCond.value(dotNetNamedElement))
			{
				continue;
			}

			int weight = myWeightProcessor.getWeight(dotNetNamedElement);
			add(new ResolveResultWithWeight(dotNetNamedElement, weight));
			if(weight == WeightProcessor.MAX_WEIGHT)
			{
				return false;
			}
		}
		return true;
	}
}
