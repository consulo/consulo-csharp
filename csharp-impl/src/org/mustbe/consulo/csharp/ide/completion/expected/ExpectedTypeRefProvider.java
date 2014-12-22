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

package org.mustbe.consulo.csharp.ide.completion.expected;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;

/**
 * @author VISTALL
 * @since 22.12.14
 */
public class ExpectedTypeRefProvider
{
	@NotNull
	public static List<ExpectedTypeInfo> findExpectedTypeRefs(@NotNull PsiElement psiElement)
	{
		PsiElement parent = psiElement.getParent();
		// <caret>;
		if(parent instanceof DotNetStatement)
		{
			return Collections.emptyList();
		}

		List<ExpectedTypeInfo> typeRefs = new SmartList<ExpectedTypeInfo>();

		if(parent instanceof CSharpAssignmentExpressionImpl)
		{
			CSharpAssignmentExpressionImpl assignmentExpression = (CSharpAssignmentExpressionImpl) parent;
			DotNetExpression[] expressions = assignmentExpression.getParameterExpressions();
			// <caret> = test;
			if(expressions[0] == assignmentExpression || expressions.length == 1)
			{
				return Collections.emptyList();
			}

			CSharpOperatorReferenceImpl operatorElement = assignmentExpression.getOperatorElement();

			ResolveResult[] resolveResults = operatorElement.multiResolve(false);

			for(ResolveResult resolveResult : resolveResults)
			{
				PsiElement element = resolveResult.getElement();
				// stub variant
				if(operatorElement == element)
				{
					PsiElement typeProvider = null;
					DotNetExpression expression = expressions[0];
					if(expression instanceof CSharpReferenceExpression)
					{
						typeProvider = ((CSharpReferenceExpression) expression).resolve();
					}
					typeRefs.add(new ExpectedTypeInfo(expression.toTypeRef(false), typeProvider));
				}
			}
		}
		else if(parent instanceof DotNetVariable)
		{
			typeRefs.add(new ExpectedTypeInfo(((DotNetVariable) parent).toTypeRef(false), parent));
		}
		else if(parent instanceof CSharpCallArgument)
		{
			CSharpCallArgumentListOwner callArgumentListOwner = PsiTreeUtil.getParentOfType(parent, CSharpCallArgumentListOwner.class);

			assert callArgumentListOwner != null;

			ResolveResult[] resolveResults = callArgumentListOwner.multiResolve(false);
			for(ResolveResult resolveResult : resolveResults)
			{
				if(resolveResult instanceof MethodResolveResult)
				{
					MethodCalcResult calcResult = ((MethodResolveResult) resolveResult).getCalcResult();

					for(NCallArgument nCallArgument : calcResult.getArguments())
					{
						if(nCallArgument.getCallArgument() == parent)
						{
							DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
							if(parameterTypeRef == null)
							{
								continue;
							}
							typeRefs.add(new ExpectedTypeInfo(parameterTypeRef, resolveResult.getElement()));
						}
					}
				}
			}
		}

		return typeRefs;
	}
}
