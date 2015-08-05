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
import org.mustbe.consulo.RequiredDispatchThread;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.Evaluator;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.IsExpressionEvaluator;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.LocalVariableEvaluator;
import org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator.ParameterEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIsExpressionImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 06.06.14
 */
public class CSharpExpressionEvaluator extends CSharpElementVisitor
{
	private List<Evaluator> myEvaluators = new ArrayList<Evaluator>();

	@Override
	@RequiredDispatchThread
	public void visitIsExpression(CSharpIsExpressionImpl expression)
	{
		DotNetExpression leftExpression = expression.getExpression();
		leftExpression.accept(this);

		myEvaluators.add(new IsExpressionEvaluator(expression));
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		PsiElement qualifier = expression.getQualifier();
		if(qualifier == null)
		{
			PsiElement resolve = expression.resolve();
			if(resolve instanceof DotNetParameter)
			{
				myEvaluators.add(new ParameterEvaluator((DotNetParameter)resolve));
			}
			else if(resolve instanceof CSharpLocalVariable)
			{
				myEvaluators.add(new LocalVariableEvaluator((CSharpLocalVariable)resolve));
			}
		}
		else
		{
			qualifier.accept(this);
		}
	}

	@Override
	public void visitElement(PsiElement element)
	{
		throw new UnsupportedOperationException("Expression is not supported");
	}

	@NotNull
	public List<Evaluator> getEvaluators()
	{
		return myEvaluators;
	}
}
