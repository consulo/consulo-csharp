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

package org.mustbe.consulo.csharp.ide.debugger;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.*;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFieldDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBinaryExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIsExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPrefixExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;

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
		PsiElement element = expression.toTypeRef(true).resolve(expression).getElement();
		if(!(element instanceof CSharpTypeDeclaration))
		{
			throw new IllegalArgumentException("bad constant type");
		}
		myEvaluators.add(new ConstantEvaluator(expression.getValue(), ((CSharpTypeDeclaration) element).getVmQName()));
	}

	@Override
	@RequiredReadAction
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
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
					myEvaluators.add(NullValueEvaluator.INSTANCE); // push null
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
				myEvaluators.add(NullValueEvaluator.INSTANCE);
			}
			else
			{
				throw new IllegalArgumentException("cant evaluate expression");
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
			throw new UnsupportedOperationException("cant evaluate expression");
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
					myEvaluators.add(NullValueEvaluator.INSTANCE); // push null
				}
				else
				{
					myEvaluators.add(ThisObjectEvaluator.INSTANCE);
				}
			}

			String referenceName = ((CSharpReferenceExpression) callExpression).getReferenceName();
			if(referenceName == null)
			{
				throw new UnsupportedOperationException("no reference name");
			}

			List<NCallArgument> arguments = ((MethodResolveResult) resolveResult).getCalcResult().getArguments();
			for(NCallArgument argument : arguments)
			{
				CSharpCallArgument callArgument = argument.getCallArgument();
				if(callArgument == null)
				{
					throw new UnsupportedOperationException("bad method call");
				}
				DotNetExpression argumentExpression = callArgument.getArgumentExpression();
				if(argumentExpression == null)
				{
					throw new UnsupportedOperationException("bad method call argument");
				}

				argumentExpression.accept(this);
			}

			pushMethodEvaluator(expression, methodDeclaration, typeDeclaration, referenceName);
		}
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
			PsiElement element = parameterTypeRef.resolve(scope).getElement();
			if(!(element instanceof CSharpTypeDeclaration))
			{
				throw new UnsupportedOperationException("parameter type is not type");
			}
			parameterTypes.add((DotNetTypeDeclaration) element);
		}
		myEvaluators.add(new MethodEvaluator(referenceName, typeDeclaration, parameterTypes));
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
			if(!element.isPhysical())
			{

			}
			else if(element instanceof CSharpMethodDeclaration)
			{
				myEvaluators.add(NullValueEvaluator.INSTANCE); // operators always static

				pushArguments(expression);

				CSharpMethodDeclaration methodDeclaration = (CSharpMethodDeclaration) element;

				pushMethodEvaluator(expression, methodDeclaration, (CSharpTypeDeclaration) element.getParent(), getMethodName(operatorElementType, methodDeclaration.getName()));

				if(operatorElementType == CSharpTokens.NTEQ)
				{
					myEvaluators.add(new PrefixOperatorEvaluator(CSharpTokens.EXCL));
				}
				return;
			}
		}
		else
		{
			throw new IllegalArgumentException("cant evaluate expression");
		}

		throw new UnsupportedOperationException("expression is not supported");
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
						throw new UnsupportedOperationException("bad operator call argument");
					}

					argumentExpression.accept(this);
				}

				myEvaluators.add(new PrefixOperatorEvaluator(operatorElement.getOperatorElementType()));
				return;
			}
		}

		throw new UnsupportedOperationException("expression is not supported");
	}

	@Override
	public void visitElement(PsiElement element)
	{
		throw new UnsupportedOperationException("expression is not supported");
	}

	@NotNull
	public List<Evaluator> getEvaluators()
	{
		return myEvaluators;
	}
}
