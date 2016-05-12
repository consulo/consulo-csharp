/*
 * Copyright 2013-2015 must-be.org
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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import org.mustbe.consulo.csharp.lang.psi.impl.DotNetTypes2;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 06.03.2015
 */
public class ExpectedTypeVisitor extends CSharpElementVisitor
{
	public static final Key<List<ExpectedTypeInfo>> EXPECTED_TYPE_INFOS = Key.create("ExpectedTypeInfo");
	@NotNull
	public static List<ExpectedTypeInfo> findExpectedTypeRefs(@NotNull PsiElement psiElement)
	{
		PsiElement parent = psiElement.getParent();

		ExpectedTypeVisitor expectedTypeVisitor = new ExpectedTypeVisitor(psiElement);

		parent.accept(expectedTypeVisitor);

		return ContainerUtil.filter(expectedTypeVisitor.getExpectedTypeInfos(), new Condition<ExpectedTypeInfo>()
		{
			@Override
			public boolean value(ExpectedTypeInfo expectedTypeInfo)
			{
				return expectedTypeInfo.getTypeRef() != DotNetTypeRef.ERROR_TYPE;
			}
		});
	}

	private List<ExpectedTypeInfo> myExpectedTypeInfos = new SmartList<ExpectedTypeInfo>();

	private PsiElement myCurrentElement;

	public ExpectedTypeVisitor(PsiElement currentElement)
	{
		myCurrentElement = currentElement;
	}

	public List<ExpectedTypeInfo> getExpectedTypeInfos()
	{
		return myExpectedTypeInfos;
	}

	@Override
	@RequiredReadAction
	public void visitIfStatement(CSharpIfStatementImpl statement)
	{
		DotNetExpression conditionExpression = statement.getConditionExpression();
		if(conditionExpression == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(statement, DotNetTypes.System.Boolean), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitAttribute(CSharpAttribute attribute)
	{
		if(attribute.getReferenceExpression() == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(attribute, DotNetTypes.System.Attribute), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitWhileStatement(CSharpWhileStatementImpl statement)
	{
		DotNetExpression conditionExpression = statement.getConditionExpression();
		if(conditionExpression == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(statement, DotNetTypes.System.Boolean), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitThrowStatement(CSharpThrowStatementImpl statement)
	{
		DotNetExpression throwExpression = statement.getExpression();
		if(throwExpression == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(statement, DotNetTypes.System.Exception), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitDoWhileStatement(CSharpDoWhileStatementImpl statement)
	{
		DotNetExpression conditionExpression = statement.getConditionExpression();
		if(conditionExpression == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(statement, DotNetTypes.System.Boolean), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitForeachStatement(CSharpForeachStatementImpl statement)
	{
		if(statement.getIterableExpression() == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(statement, DotNetTypes2.System.Collections.IEnumerable), null));
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(statement, DotNetTypes2.System.Collections.Generic.IEnumerable$1), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitNamedFieldOrPropertySet(CSharpNamedFieldOrPropertySet parent)
	{
		CSharpReferenceExpression nameReferenceExpression = parent.getNameElement();
		DotNetExpression valueExpression = parent.getValueExpression();

		if(nameReferenceExpression == myCurrentElement)
		{
			if(valueExpression != null)
			{
				myExpectedTypeInfos.add(new ExpectedTypeInfo(valueExpression.toTypeRef(false), null));
			}
		}
		else if(valueExpression == myCurrentElement)
		{
			PsiElement resolvedElement = nameReferenceExpression.resolve();
			if(resolvedElement instanceof DotNetVariable)
			{
				myExpectedTypeInfos.add(new ExpectedTypeInfo(((DotNetVariable) resolvedElement).toTypeRef(true), resolvedElement));
			}
		}
	}

	@Override
	@RequiredReadAction
	public void visitReturnStatement(CSharpReturnStatementImpl statement)
	{
		CSharpSimpleLikeMethodAsElement methodAsElement = PsiTreeUtil.getParentOfType(myCurrentElement, CSharpSimpleLikeMethodAsElement.class);
		if(methodAsElement == null)
		{
			return;
		}

		CSharpImplicitReturnModel implicitReturnModel = CSharpImplicitReturnModel.getImplicitReturnModel(statement, methodAsElement);

		DotNetTypeRef extractedTypeRef = implicitReturnModel.extractTypeRef(methodAsElement.getReturnTypeRef(), statement);
		myExpectedTypeInfos.add(new ExpectedTypeInfo(extractedTypeRef, methodAsElement));
	}

	@Override
	@RequiredReadAction
	public void visitRefTypeExpression(CSharpRefTypeExpressionImpl expression)
	{
		myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(expression, DotNetTypes.System.TypedReference), null));
	}

	@Override
	@RequiredReadAction
	public void visitRefValueExpression(CSharpRefValueExpressionImpl parent)
	{
		if(parent.getExpression() == myCurrentElement)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(parent, DotNetTypes.System.TypedReference), null));
		}
	}

	@Override
	@RequiredReadAction
	public void visitAssignmentExpression(CSharpAssignmentExpressionImpl parent)
	{
		DotNetExpression[] expressions = parent.getParameterExpressions();
		// <caret> = test;
		if(expressions.length == 1)
		{
			return;
		}
		if(expressions[0] == myCurrentElement)
		{
			DotNetExpression rightExpression = expressions[1];
			DotNetTypeRef typeRef = rightExpression.toTypeRef(true);
			myExpectedTypeInfos.add(new ExpectedTypeInfo(typeRef, null));
		}
		else
		{
			CSharpOperatorReferenceImpl operatorElement = parent.getOperatorElement();

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
					myExpectedTypeInfos.add(new ExpectedTypeInfo(expression.toTypeRef(false), typeProvider));
				}
				else if(element instanceof CSharpMethodDeclaration)
				{
					if(((CSharpMethodDeclaration) element).isOperator())
					{
						DotNetParameter[] parameters = ((CSharpMethodDeclaration) element).getParameters();
						DotNetParameter parameter = ArrayUtil2.safeGet(parameters, 1);
						if(parameter == null)
						{
							return;
						}
						myExpectedTypeInfos.add(new ExpectedTypeInfo(parameter.toTypeRef(true), element));
					}
				}
			}
		}
	}

	@Override
	@RequiredReadAction
	public void visitAwaitExpression(CSharpAwaitExpressionImpl expression)
	{
		myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(expression, DotNetTypes2.System.Threading.Tasks.Task), null));
		myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(expression, DotNetTypes2.System.Threading.Tasks.Task$1), null));
	}

	@Override
	@RequiredReadAction
	public void visitVariable(DotNetVariable variable)
	{
		myExpectedTypeInfos.add(new ExpectedTypeInfo(variable.toTypeRef(false), variable));
	}

	@Override
	public void visitCallArgument(CSharpCallArgument argument)
	{
		CSharpCallArgumentListOwner callArgumentListOwner = PsiTreeUtil.getParentOfType(argument, CSharpCallArgumentListOwner.class, false);

		assert callArgumentListOwner != null;

		ResolveResult[] resolveResults = callArgumentListOwner.multiResolve(false);
		for(ResolveResult resolveResult : resolveResults)
		{
			if(resolveResult instanceof MethodResolveResult)
			{
				MethodCalcResult calcResult = ((MethodResolveResult) resolveResult).getCalcResult();

				for(NCallArgument nCallArgument : calcResult.getArguments())
				{
					if(nCallArgument.getCallArgument() == argument)
					{
						DotNetTypeRef parameterTypeRef = nCallArgument.getParameterTypeRef();
						if(parameterTypeRef == null)
						{
							continue;
						}
						myExpectedTypeInfos.add(new ExpectedTypeInfo(parameterTypeRef, resolveResult.getElement()));
					}
				}
			}
		}
	}

	@Override
	public void visitNamedCallArgument(CSharpNamedCallArgument argument)
	{
		visitCallArgument(argument);
	}

	@Override
	@RequiredReadAction
	public void visitUserType(CSharpUserType parent)
	{
		PsiElement parentOfUserType = parent.getParent();
		if(parentOfUserType instanceof CSharpAsExpressionImpl ||
				parentOfUserType instanceof CSharpTypeCastExpressionImpl ||
				parentOfUserType instanceof CSharpNewExpression ||
				parentOfUserType instanceof CSharpRefValueExpressionImpl)
		{
			myExpectedTypeInfos.addAll(findExpectedTypeRefs(parentOfUserType));
		}
		else if(parentOfUserType instanceof CSharpLocalVariable && parentOfUserType.getParent() instanceof CSharpCatchStatementImpl)
		{
			myExpectedTypeInfos.add(new ExpectedTypeInfo(new CSharpTypeRefByQName(parent, DotNetTypes.System.Exception), null));
		}
	}

	@Override
	public void visitOurRefWrapExpression(CSharpOutRefWrapExpressionImpl expression)
	{
		myExpectedTypeInfos.addAll(findExpectedTypeRefs(expression));
	}
}
