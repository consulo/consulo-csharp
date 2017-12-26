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

package consulo.csharp.ide.debugger;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.debugger.expressionEvaluator.*;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpBinaryExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpIndexAccessExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpIsExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import consulo.csharp.lang.psi.impl.source.CSharpPrefixExpressionImpl;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class CSharpExpressionEvaluator extends CSharpElementVisitor
{
	private List<Evaluator> myEvaluators = new ArrayList<Evaluator>();

	@Override
	@RequiredReadAction
	public void visitIsExpression(CSharpIsExpressionImpl expression)
	{
		DotNetExpression leftExpression = expression.getExpression();
		leftExpression.accept(this);

		myEvaluators.add(new IsExpressionEvaluator(expression));
	}

	@Override
	@RequiredReadAction
	public void visitConstantExpression(CSharpConstantExpressionImpl expression)
	{
		PsiElement element = expression.toTypeRef(true).resolve().getElement();
		if(!(element instanceof CSharpTypeDeclaration))
		{
			cantEvaluateExpression();
		}
		myEvaluators.add(new ConstantEvaluator(expression.getValue(), ((CSharpTypeDeclaration) element).getVmQName()));
	}

	@Override
	@RequiredReadAction
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		CSharpReferenceExpression.ResolveToKind kind = expression.kind();
		PsiElement qualifier = expression.getQualifier();
		if(qualifier == null)
		{
			PsiElement resolvedElement = expression.resolve();
			if(resolvedElement instanceof DotNetParameter)
			{
				myEvaluators.add(new ParameterEvaluator((DotNetParameter) resolvedElement));
			}
			else if(resolvedElement instanceof CSharpLocalVariable)
			{
				myEvaluators.add(new LocalVariableEvaluator((CSharpLocalVariable) resolvedElement));
			}
			else if(resolvedElement instanceof CSharpFieldDeclaration || resolvedElement instanceof CSharpPropertyDeclaration)
			{
				CSharpTypeDeclaration typeDeclaration = null;
				if(((DotNetModifierListOwner) resolvedElement).hasModifier(DotNetModifier.STATIC))
				{
					typeDeclaration = (CSharpTypeDeclaration) resolvedElement.getParent();
					myEvaluators.add(StaticObjectEvaluator.INSTANCE);
				}
				else
				{
					myEvaluators.add(ThisObjectEvaluator.INSTANCE);
				}

				if(resolvedElement instanceof CSharpPropertyDeclaration)
				{
					myEvaluators.add(new PropertyEvaluator(typeDeclaration, (CSharpPropertyDeclaration) resolvedElement));
				}
				else if(resolvedElement instanceof CSharpFieldDeclaration)
				{
					myEvaluators.add(new FieldEvaluator(typeDeclaration, (CSharpFieldDeclaration) resolvedElement));
				}
			}
			else if(resolvedElement instanceof CSharpTypeDeclaration)
			{
				switch(kind)
				{
					case THIS:
						myEvaluators.add(ThisObjectEvaluator.INSTANCE);
						break;
					default:
						myEvaluators.add(StaticObjectEvaluator.INSTANCE);
						break;
				}
			}
			else
			{
				cantEvaluateExpression();
			}
		}
		else
		{
			qualifier.accept(this);

			PsiElement resolvedElement = expression.resolve();
			if(resolvedElement instanceof CSharpFieldDeclaration)
			{
				CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) resolvedElement.getParent();
				myEvaluators.add(new FieldEvaluator(typeDeclaration, (CSharpFieldDeclaration) resolvedElement));
			}
			else if(resolvedElement instanceof CSharpPropertyDeclaration)
			{
				CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) resolvedElement.getParent();
				myEvaluators.add(new PropertyEvaluator(typeDeclaration, (CSharpPropertyDeclaration) resolvedElement));
			}
			else
			{
				cantEvaluateExpression();
			}
		}
	}

	@Override
	@RequiredReadAction
	public void visitMethodCallExpression(CSharpMethodCallExpressionImpl expression)
	{
		DotNetExpression callExpression = expression.getCallExpression();

		ResolveResult resolveResult = CSharpResolveUtil.findFirstValidResult(expression.multiResolve(false));
		if(resolveResult == null || !(resolveResult instanceof MethodResolveResult) || !(resolveResult.getElement() instanceof CSharpMethodDeclaration))
		{
			cantEvaluateExpression();
		}

		CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) resolveResult.getElement();

		if(callExpression instanceof CSharpReferenceExpression)
		{
			CSharpTypeDeclaration typeDeclaration = null;

			PsiElement qualifier = ((CSharpReferenceExpression) callExpression).getQualifier();
			if(qualifier != null)
			{
				qualifier.accept(this);
			}
			else
			{
				if(methodDeclaration.hasModifier(DotNetModifier.STATIC))
				{
					typeDeclaration = (CSharpTypeDeclaration) methodDeclaration.getParent();
					myEvaluators.add(StaticObjectEvaluator.INSTANCE);
				}
				else
				{
					myEvaluators.add(ThisObjectEvaluator.INSTANCE);
				}
			}

			String referenceName = ((CSharpReferenceExpression) callExpression).getReferenceName();
			if(referenceName == null)
			{
				cantEvaluateExpression();
			}

			pushNArguments((MethodResolveResult) resolveResult);

			pushMethodEvaluator(expression, methodDeclaration, typeDeclaration, referenceName);
		}
	}

	@Override
	@RequiredReadAction
	public void visitBinaryExpression(CSharpBinaryExpressionImpl expression)
	{
		CSharpOperatorReferenceImpl operatorElement = expression.getOperatorElement();

		IElementType operatorElementType = expression.getOperatorElement().getOperatorElementType();

		PsiElement element = operatorElement.resolveToCallable();
		if(element != null)
		{
			myEvaluators.add(StaticObjectEvaluator.INSTANCE); // operators always static

			pushArguments(expression);

			CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;

			pushMethodEvaluator(expression, methodDeclaration, (CSharpTypeDeclaration) element.getParent(), getMethodName(operatorElementType, methodDeclaration.getName()));

			if(operatorElementType == CSharpTokens.NTEQ)
			{
				myEvaluators.add(new PrefixOperatorEvaluator(CSharpTokens.EXCL));
			}
			return;
		}
		else
		{
			cantEvaluateExpression();
		}

		expressionIsNotSupported();
	}

	@Override
	@RequiredReadAction
	public void visitPrefixExpression(CSharpPrefixExpressionImpl expression)
	{
		CSharpOperatorReferenceImpl operatorElement = expression.getOperatorElement();

		PsiElement element = operatorElement.resolveToCallable();
		if(element != null)
		{
			if(!element.isPhysical())
			{
				CSharpCallArgument[] callArguments = expression.getCallArguments();
				for(CSharpCallArgument callArgument : callArguments)
				{
					DotNetExpression argumentExpression = callArgument.getArgumentExpression();
					if(argumentExpression == null)
					{
						cantEvaluateExpression();
					}

					argumentExpression.accept(this);
				}

				myEvaluators.add(new PrefixOperatorEvaluator(operatorElement.getOperatorElementType()));
				return;
			}
		}

		expressionIsNotSupported();
	}

	@Override
	@RequiredReadAction
	public void visitIndexAccessExpression(CSharpIndexAccessExpressionImpl expression)
	{
		PsiElement parent = expression.getParent();
		if(parent instanceof CSharpAssignmentExpressionImpl && ((CSharpAssignmentExpressionImpl) parent).getCallArguments()[0] == expression)
		{
			expressionIsNotSupported();
		}

		ResolveResult resolveResult = CSharpResolveUtil.findFirstValidResult(expression.multiResolve(false));
		if(resolveResult == null || !(resolveResult instanceof MethodResolveResult) || !(resolveResult.getElement() instanceof CSharpIndexMethodDeclaration))
		{
			cantEvaluateExpression();
		}

		CSharpIndexMethodDeclaration indexMethodDeclaration = (CSharpIndexMethodDeclaration) resolveResult.getElement();

		expression.getQualifier().accept(this);

		pushNArguments((MethodResolveResult) resolveResult);

		DotNetTypeRef[] parameterTypeRefs = indexMethodDeclaration.getParameterTypeRefs();
		List<DotNetTypeDeclaration> parameterTypes = new ArrayList<DotNetTypeDeclaration>();
		for(DotNetTypeRef parameterTypeRef : parameterTypeRefs)
		{
			PsiElement element = parameterTypeRef.resolve().getElement();
			if(!(element instanceof CSharpTypeDeclaration))
			{
				throw new UnsupportedOperationException("parameter type is not type");
			}
			parameterTypes.add((DotNetTypeDeclaration) element);
		}

		myEvaluators.add(new IndexMethodEvaluator(indexMethodDeclaration, parameterTypes));
	}

	@Override
	public void visitElement(PsiElement element)
	{
		expressionIsNotSupported();
	}

	@Contract(value = "_ -> fail")
	private static void cantEvaluateExpression()
	{
		throw new IllegalArgumentException("cant evaluate expression");
	}

	private static void expressionIsNotSupported()
	{
		throw new UnsupportedOperationException("expression is not supported");
	}

	@NotNull
	private static String getMethodName(IElementType elementType, String originalName)
	{
		if(elementType == CSharpTokens.EQEQ || elementType == CSharpTokens.NTEQ)
		{
			return "Equals";
		}
		return originalName;
	}

	@RequiredReadAction
	private void pushMethodEvaluator(PsiElement scope, CSharpMethodDeclaration methodDeclaration, CSharpTypeDeclaration typeDeclaration, @NotNull String referenceName)
	{
		DotNetTypeRef[] parameterTypeRefs = methodDeclaration.getParameterTypeRefs();
		List<DotNetTypeDeclaration> parameterTypes = new ArrayList<DotNetTypeDeclaration>();
		for(DotNetTypeRef parameterTypeRef : parameterTypeRefs)
		{
			PsiElement element = parameterTypeRef.resolve().getElement();
			if(!(element instanceof CSharpTypeDeclaration))
			{
				throw new UnsupportedOperationException("parameter type is not type");
			}
			parameterTypes.add((DotNetTypeDeclaration) element);
		}
		myEvaluators.add(new MethodEvaluator(referenceName, typeDeclaration, parameterTypes));
	}

	@RequiredReadAction
	private void pushNArguments(MethodResolveResult resolveResult)
	{
		List<NCallArgument> arguments = resolveResult.getCalcResult().getArguments();
		for(NCallArgument argument : arguments)
		{
			CSharpCallArgument callArgument = argument.getCallArgument();
			if(callArgument == null)
			{
				cantEvaluateExpression();
			}
			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression == null)
			{
				cantEvaluateExpression();
			}

			argumentExpression.accept(this);
		}
	}

	private void pushArguments(CSharpBinaryExpressionImpl expression)
	{
		CSharpCallArgument[] callArguments = expression.getCallArguments();
		for(CSharpCallArgument callArgument : callArguments)
		{
			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression == null)
			{
				throw new UnsupportedOperationException("bad operator call argument");
			}

			argumentExpression.accept(this);
		}
	}

	@NotNull
	public List<Evaluator> getEvaluators()
	{
		return myEvaluators;
	}
}
