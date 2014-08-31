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

package org.mustbe.consulo.csharp.ide.cfs;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.cfs.lang.CfsLanguage;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.lang.injection.MultiHostInjector;
import com.intellij.lang.injection.MultiHostRegistrar;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;

/**
 * @author VISTALL
 * @since 31.08.14
 */
public class CfsMultiHostInjector implements MultiHostInjector
{
	@Override
	public void injectLanguages(@NotNull MultiHostRegistrar multiHostRegistrar, @NotNull PsiElement element)
	{
		CSharpConstantExpressionImpl host = (CSharpConstantExpressionImpl) element;
		if(!host.isValidHost())
		{
			return;
		}

		PsiElement parent = element.getParent();
		if(!(parent instanceof CSharpCallArgumentList))
		{
			return;
		}

		CSharpCallArgumentListOwner owner = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);
		if(owner == null)
		{
			return;
		}

		PsiElement psiElement = owner.resolveToCallable();
		if(!(psiElement instanceof DotNetLikeMethodDeclaration))
		{
			return;
		}

		DotNetAttribute attribute = DotNetAttributeUtil.findAttribute(psiElement, "MustBe.Consulo.Attributes.CompositeFormattingMethodAttribute");
		if(attribute == null)
		{
			return;
		}

		if(!(attribute instanceof CSharpAttribute))
		{
			return;
		}

		DotNetExpression[] parameterExpressions = ((CSharpAttribute) attribute).getParameterExpressions();
		if(parameterExpressions.length == 0)
		{
			return;
		}

		String value = new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
		if(value == null)
		{
			return;
		}

		int expressionIndex = ArrayUtil.indexOf(owner.getParameterExpressions(), element);
		if(expressionIndex == -1)
		{
			return;
		}

		int parameterIndex = -1;
		DotNetParameter[] parameters = ((DotNetLikeMethodDeclaration) psiElement).getParameters();
		for(int j = 0; j < parameters.length; j++)
		{
			DotNetParameter parameter = parameters[j];
			if(Comparing.equal(parameter.getName(), value))
			{
				parameterIndex = j;
				break;
			}
		}

		if(parameterIndex != expressionIndex)
		{
			return;
		}

		int firstIndex = 1;
		if(host.getLiteralType() == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			firstIndex = 2;
		}
		multiHostRegistrar.startInjecting(CfsLanguage.INSTANCE).addPlace("", "", host, new TextRange(firstIndex,
				element.getTextLength() - 1)).doneInjecting();
	}
}
