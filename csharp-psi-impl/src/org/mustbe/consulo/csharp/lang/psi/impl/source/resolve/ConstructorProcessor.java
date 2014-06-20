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
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpPseudoMethod;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightParameterBuilder;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.ResolveState;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class ConstructorProcessor extends AbstractScopeProcessor
{
	private WeightProcessor<PsiElement> myWeightProcessor;

	@SuppressWarnings("unchecked")
	public ConstructorProcessor(final CSharpCallArgumentListOwner parent, boolean completion)
	{
		myWeightProcessor = completion ? WeightProcessor.MAXIMUM : new WeightProcessor<PsiElement>()
		{
			@Override
			public int getWeight(@NotNull PsiElement psiNamedElement)
			{
				if(psiNamedElement instanceof DotNetConstructorDeclaration)
				{
					return MethodAcceptorImpl.calcAcceptableWeight(parent, (DotNetConstructorDeclaration) psiNamedElement);
				}
				return 0;
			}
		};
	}

	@Override
	public boolean executeImpl(@NotNull PsiElement element, ResolveState state)
	{
		if(element instanceof DotNetConstructorDeclaration && !((DotNetConstructorDeclaration) element).isDeConstructor())
		{
			addElement(element, myWeightProcessor.getWeight(element));
		}
		return true;
	}

	public void executeDefault(PsiNamedElement owner)
	{
		if(!isEmpty())
		{
			return;
		}

		CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(owner.getProject());
		builder.addModifier(CSharpModifier.PUBLIC);
		builder.setNavigationElement(owner);
		builder.withParent(owner);
		builder.withName(owner.getName());

		if(owner instanceof CSharpPseudoMethod)
		{
			CSharpLightParameterBuilder parameter = new CSharpLightParameterBuilder(owner.getProject());
			parameter = parameter.withName("p");
			parameter = parameter.withTypeRef(new CSharpLambdaTypeRef(null, ((CSharpPseudoMethod) owner).getParameterTypeRefs(),
					((CSharpPseudoMethod) owner).getReturnTypeRef()));
			builder.addParameter(parameter);
		}

		execute(builder, null);
	}
}
