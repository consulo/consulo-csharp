/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.impl.ide.completion.expected;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpImplicitReturnModel;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.source.*;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ContainerUtil;
import consulo.util.dataholder.Key;

import jakarta.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 06.03.2015
 */
public class ExpectedTypeVisitor extends CSharpElementVisitor
{
	public static final Key<List<ExpectedTypeInfo>> EXPECTED_TYPE_INFOS = Key.create("ExpectedTypeInfo");

	@Nonnull
	public static List<ExpectedTypeInfo> findExpectedTypeRefs(@Nonnull PsiElement psiElement)
	{
		if(!psiElement.isValid())
		{
			return List.of();
		}

		PsiElement parent = psiElement.getParent();
		if(parent == null)
		{
			return List.of();
		}

		return LanguageCachedValueUtil.getCachedValue(psiElement, () -> CachedValueProvider.Result.create(findExpectedTypeRefsImpl(psiElement), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	private static List<ExpectedTypeInfo> findExpectedTypeRefsImpl(PsiElement psiElement)
	{
		PsiElement parent = psiElement.getParent();
		if(parent == null)
		{
			return List.of();
		}

		ExpectedTypeVisitor expectedTypeVisitor = new ExpectedTypeVisitor(psiElement);

		parent.accept(expectedTypeVisitor);

		return ContainerUtil.filter(expectedTypeVisitor.getExpectedTypeInfos(), it -> it.getTypeRef() != DotNetTypeRef.ERROR_TYPE);
	}

	private List<ExpectedTypeInfo> myExpectedTypeInfos = new ArrayList<>();

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
	public void visitBinaryExpression(CSharpBinaryExpressionImpl expression)
	{
		IElementType elementType = expression.getOperatorElement().getOperatorElementType();
		if(elementType != CSharpTokens.EQEQ && elementType != CSharpTokens.NTEQ)
		{
			return;
		}

		DotNetExpression targetElement = expression.getRightExpression();

		if(myCurrentElement == targetElement)
		{
			targetElement = expression.getLeftExpression();
		}

		if(targetElement == null)
		{
			return;
		}
		
		myExpectedTypeInfos.add(new ExpectedTypeInfo(targetElement.toTypeRef(true), null));
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

			ResolveResult[] resolveResults = operatorElement.multiResolve(true);

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

		ResolveResult[] resolveResults = callArgumentListOwner.multiResolve(true);
		for(ResolveResult resolveResult : resolveResults)
		{
			if(resolveResult instanceof MethodResolveResult)
			{
				MethodResolvePriorityInfo calcResult = ((MethodResolveResult) resolveResult).getCalcResult();

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
