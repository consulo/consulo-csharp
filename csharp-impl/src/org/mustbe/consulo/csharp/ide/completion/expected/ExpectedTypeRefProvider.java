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
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldOrPropertySet;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import org.mustbe.consulo.csharp.lang.psi.CSharpUserType;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import org.mustbe.consulo.csharp.lang.psi.impl.DotNetTypes2;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Condition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

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

		List<ExpectedTypeInfo> infoList = new SmartList<ExpectedTypeInfo>();
		if(parent instanceof CSharpIfStatementImpl)
		{
			DotNetExpression conditionExpression = ((CSharpIfStatementImpl) parent).getConditionExpression();
			if(conditionExpression == psiElement)
			{
				infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes.System.Boolean), null));
			}
		}
		else if(parent instanceof CSharpAttribute)
		{
			if(((CSharpAttribute) parent).getReferenceExpression() == psiElement)
			{
				infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes.System.Attribute), null));
			}
		}
		else if(parent instanceof CSharpWhileStatementImpl)
		{
			DotNetExpression conditionExpression = ((CSharpWhileStatementImpl) parent).getConditionExpression();
			if(conditionExpression == psiElement)
			{
				infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes.System.Boolean), null));
			}
		}
		else if(parent instanceof CSharpDoWhileStatementImpl)
		{
			DotNetExpression conditionExpression = ((CSharpDoWhileStatementImpl) parent).getConditionExpression();
			if(conditionExpression == psiElement)
			{
				infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes.System.Boolean), null));
			}
		}
		else if(parent instanceof CSharpForeachStatementImpl)
		{
			if(((CSharpForeachStatementImpl) parent).getIterableExpression() == psiElement)
			{
				infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes2.System.Collections.IEnumerable), null));
				infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes2.System.Collections.Generic.IEnumerable$1), null));
			}
		}
		else if(parent instanceof CSharpFieldOrPropertySet)
		{
			CSharpReferenceExpression nameReferenceExpression = ((CSharpFieldOrPropertySet) parent).getNameReferenceExpression();
			DotNetExpression valueExpression = ((CSharpFieldOrPropertySet) parent).getValueExpression();

			if(nameReferenceExpression == psiElement)
			{
				if(valueExpression != null)
				{
					infoList.add(new ExpectedTypeInfo(valueExpression.toTypeRef(false), null));
				}
			}
			else if(valueExpression == psiElement)
			{
				PsiElement resolvedElement = nameReferenceExpression.resolve();
				if(resolvedElement instanceof DotNetVariable)
				{
					infoList.add(new ExpectedTypeInfo(((DotNetVariable) resolvedElement).toTypeRef(true), resolvedElement));
				}
			}
		}
		else if(parent instanceof CSharpReturnStatementImpl)
		{
			CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(psiElement, CSharpSimpleLikeMethodAsElement.class);
			if(methodAsElement == null)
			{
				return Collections.emptyList();
			}

			val implicitReturnModel = CSharpImplicitReturnModel.getImplicitReturnModel((CSharpReturnStatementImpl) parent, methodAsElement);

			DotNetTypeRef extractedTypeRef = implicitReturnModel.extractTypeRef(methodAsElement.getReturnTypeRef(), parent);
			infoList.add(new ExpectedTypeInfo(extractedTypeRef, methodAsElement));
		}
		else if(parent instanceof CSharpAssignmentExpressionImpl)
		{
			CSharpAssignmentExpressionImpl assignmentExpression = (CSharpAssignmentExpressionImpl) parent;
			DotNetExpression[] expressions = assignmentExpression.getParameterExpressions();
			// <caret> = test;
			if(expressions.length == 1)
			{
				return infoList;
			}
			if(expressions[0] == psiElement)
			{
				DotNetExpression rightExpression = expressions[1];
				DotNetTypeRef typeRef = rightExpression.toTypeRef(true);
				infoList.add(new ExpectedTypeInfo(typeRef, null));
			}
			else
			{
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
						infoList.add(new ExpectedTypeInfo(expression.toTypeRef(false), typeProvider));
					}
				}
			}
		}
		else if(parent instanceof CSharpAwaitExpressionImpl)
		{
			infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes2.System.Threading.Tasks.Task), null));
			infoList.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(DotNetTypes2.System.Threading.Tasks.Task$1), null));
		}
		else if(parent instanceof DotNetVariable)
		{
			infoList.add(new ExpectedTypeInfo(((DotNetVariable) parent).toTypeRef(false), parent));
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
							infoList.add(new ExpectedTypeInfo(parameterTypeRef, resolveResult.getElement()));
						}
					}
				}
			}
		}
		else if(parent instanceof CSharpUserType)
		{
			PsiElement parentOfUserType = parent.getParent();
			if(parentOfUserType instanceof CSharpAsExpressionImpl || parentOfUserType instanceof CSharpTypeCastExpressionImpl)
			{
				infoList.addAll(findExpectedTypeRefs(parentOfUserType));
			}
		}

		return ContainerUtil.filter(infoList, new Condition<ExpectedTypeInfo>()
		{
			@Override
			public boolean value(ExpectedTypeInfo expectedTypeInfo)
			{
				return expectedTypeInfo.getTypeRef() != DotNetTypeRef.ERROR_TYPE;
			}
		});
	}
}
