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
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightParameterList;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class ExtensionResolveScopeProcessor extends AbstractScopeProcessor
{
	private final Condition<PsiNamedElement> myCond;
	private final WeightProcessor<PsiNamedElement> myWeightProcessor;
	private final boolean myNamed;

	public ExtensionResolveScopeProcessor(Condition<PsiNamedElement> condition, WeightProcessor<PsiNamedElement> weightProcessor, boolean named)
	{
		myCond = condition;
		myWeightProcessor = weightProcessor;
		myNamed = named;

		putUserData(CSharpResolveUtil.CONDITION_KEY, new Condition<PsiElement>()
		{
			@Override
			public boolean value(PsiElement element)
			{
				return element instanceof CSharpTypeDeclaration && ((CSharpTypeDeclaration) element).hasExtensions();
			}
		});
	}

	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		assert element instanceof CSharpTypeDeclaration;

		for(DotNetNamedElement dotNetNamedElement : ((CSharpTypeDeclaration) element).getMembers())
		{
			if(!myCond.value(dotNetNamedElement) || !CSharpMethodImplUtil.isExtensionMethod(dotNetNamedElement))
			{
				continue;
			}

			int weight = myWeightProcessor.getWeight(dotNetNamedElement);
			add(new ResolveResultWithWeight(transform((CSharpMethodDeclaration) dotNetNamedElement), weight));
			if(weight == WeightProcessor.MAX_WEIGHT && myNamed)
			{
				return false;
			}
		}
		return true;
	}

	private static PsiElement transform(CSharpMethodDeclaration methodDeclaration)
	{
		DotNetParameterList parameterList = methodDeclaration.getParameterList();
		assert parameterList != null;
		DotNetParameter[] oldParameters = parameterList.getParameters();

		DotNetParameter[] parameters = new DotNetParameter[oldParameters.length - 1];
		System.arraycopy(oldParameters, 1, parameters, 0, parameters.length);

		CSharpLightParameterList lightParameterList = new CSharpLightParameterList(parameterList, parameters);
		CSharpLightMethodDeclaration declaration = new CSharpLightMethodDeclaration(methodDeclaration,
				methodDeclaration.getReturnTypeRef(), lightParameterList);
		declaration.setExtensionWrapper();
		return declaration;
	}
}
