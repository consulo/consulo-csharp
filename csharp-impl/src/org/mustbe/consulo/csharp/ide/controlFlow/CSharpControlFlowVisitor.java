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

package org.mustbe.consulo.csharp.ide.controlFlow;

import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpBindInstruction;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpInstruction;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpInstructionFactory;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpLabel;
import org.mustbe.consulo.csharp.ide.controlFlow.instruction.CSharpReturnInstruction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariableDeclarationStatement;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpAssignmentExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpIfStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 30.12.14
 */
public class CSharpControlFlowVisitor extends CSharpElementVisitor
{
	private final CSharpInstructionFactory myFactory;

	public CSharpControlFlowVisitor(CSharpInstructionFactory factory)
	{
		myFactory = factory;
	}

	@Override
	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		declaration.acceptChildren(this);

		PsiElement codeBlock = declaration.getCodeBlock();
		if(codeBlock != null)
		{
			if(DotNetTypeRefUtil.isVmQNameEqual(declaration.getReturnTypeRef(), declaration, DotNetTypes.System.Void))
			{
				CSharpInstruction last = myFactory.last();
				if(!(last instanceof CSharpReturnInstruction))
				{
					myFactory.errorValue();
					myFactory.returnValue(declaration);
				}
			}
		}
	}

	@Override
	public void visitParameterList(DotNetParameterList list)
	{
		for(DotNetParameter parameter : list.getParameters())
		{
			parameter.accept(this);
		}
	}

	@Override
	public void visitParameter(DotNetParameter parameter)
	{
		myFactory.createVariable(parameter);
	}

	@Override
	public void visitBlockStatement(CSharpBlockStatementImpl statement)
	{
		statement.acceptChildren(this);
	}

	@Override
	public void visitLocalVariableDeclarationStatement(CSharpLocalVariableDeclarationStatement statement)
	{
		for(CSharpLocalVariable localVariable : statement.getVariables())
		{
			localVariable.accept(this);
		}
	}

	@Override
	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		myFactory.createVariable(variable);

		DotNetExpression initializer = variable.getInitializer();
		if(initializer != null)
		{
			initializer.accept(this);
			myFactory.writeValue(variable);
		}
	}

	@Override
	public void visitExpressionStatement(CSharpExpressionStatementImpl statement)
	{
		DotNetExpression expression = statement.getExpression();
		if(expression != null)
		{
			expression.accept(this);
		}
	}

	@Override
	public void visitIfStatement(CSharpIfStatementImpl statement)
	{
		CSharpLabel label = myFactory.label(statement);

		try
		{
			DotNetExpression conditionExpression = statement.getConditionExpression();
			if(conditionExpression != null)
			{
				conditionExpression.accept(this);
			}

			CSharpBindInstruction mark = myFactory.bind();

			DotNetStatement trueStatement = statement.getTrueStatement();
			if(trueStatement != null)
			{
				trueStatement.accept(this);
			}

			myFactory.replace(mark).boolJump(myFactory.position());

			DotNetStatement elseStatement = statement.getElseStatement();
			if(elseStatement != null)
			{
				CSharpBindInstruction mark2 = myFactory.bind();

				elseStatement.accept(this);

				myFactory.replace(mark2).jump(myFactory.position());
			}
		}
		finally
		{
			label.finish();
		}
	}

	@Override
	public void visitAssignmentExpression(CSharpAssignmentExpressionImpl expression)
	{
		DotNetExpression[] parameterExpressions = expression.getParameterExpressions();

		DotNetExpression leftExpression = parameterExpressions[0];

		leftExpression.accept(this);

		if(parameterExpressions.length > 1)
		{
			parameterExpressions[1].accept(this);
		}
		else
		{
			myFactory.errorValue();
		}

		myFactory.writeValue(leftExpression);
	}

	@Override
	public void visitReturnStatement(CSharpReturnStatementImpl statement)
	{
		DotNetExpression expression = statement.getExpression();
		if(expression != null)
		{
			expression.accept(this);
		}
		else
		{
			myFactory.errorValue();
		}

		myFactory.returnValue(statement);
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		PsiElement qualifier = expression.getQualifier();
		if(qualifier != null)
		{
			qualifier.accept(this);
			myFactory.pop();
		}

		myFactory.readValue(expression);
	}

	@Override
	public void visitConstantExpression(CSharpConstantExpressionImpl expression)
	{
		myFactory.putConstantValue(expression);
	}
}
