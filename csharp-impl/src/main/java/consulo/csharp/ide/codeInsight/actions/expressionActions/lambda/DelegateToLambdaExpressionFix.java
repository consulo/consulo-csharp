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

package consulo.csharp.ide.codeInsight.actions.expressionActions.lambda;

import javax.annotation.Nonnull;

import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 01.11.14
 */
public class DelegateToLambdaExpressionFix extends PsiElementBaseIntentionAction
{
	@Override
	public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException
	{
		CSharpDelegateExpressionImpl delegateExpression = PsiTreeUtil.getParentOfType(element, CSharpDelegateExpressionImpl.class);

		assert delegateExpression != null;

		DotNetParameter[] parameters = delegateExpression.getParameters();

		StringBuilder builder = new StringBuilder();

		if(delegateExpression.hasModifier(CSharpModifier.ASYNC))
		{
			builder.append("async ");
		}

		if(parameters.length == 0)
		{
			builder.append("()");
		}
		else if(parameters.length == 1)
		{
			builder.append(parameters[0].getName());
		}
		else
		{
			builder.append("(");
			for(int i = 0; i < parameters.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				DotNetParameter parameter = parameters[i];
				builder.append(parameter.getName());
			}
			builder.append(")");
		}

		builder.append(" => ");

		CSharpBlockStatementImpl statement = delegateExpression.getBodyStatement();
		if(statement == null)
		{
			builder.append("{}");
		}
		else
		{
			DotNetStatement[] statements = statement.getStatements();
			if(statements.length == 1 && statements[0] instanceof CSharpReturnStatementImpl)
			{
				DotNetExpression expression = ((CSharpReturnStatementImpl) statements[0]).getExpression();
				if(expression != null)
				{
					builder.append(expression.getText());
				}
				else
				{
					builder.append("{}");
				}
			}
			else
			{
				String join = StringUtil.join(statements, new Function<DotNetStatement, String>()
				{
					@Override
					public String fun(DotNetStatement dotNetStatement)
					{
						return dotNetStatement.getText();
					}
				}, "\n");
				builder.append("{").append(join).append("}");
			}
		}

		DotNetExpression expression = CSharpFileFactory.createExpression(project, builder.toString());

		delegateExpression.replace(expression);
	}

	@Override
	public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element)
	{
		ASTNode node = element.getNode();
		if(node == null)
		{
			return false;
		}
		if(node.getElementType() == CSharpTokens.DELEGATE_KEYWORD)
		{
			CSharpDelegateExpressionImpl anonymMethodExpression = PsiTreeUtil.getParentOfType(element, CSharpDelegateExpressionImpl.class);
			return anonymMethodExpression != null && anonymMethodExpression.toTypeRef(true) != DotNetTypeRef.ERROR_TYPE;
		}
		return false;
	}

	@Nonnull
	@Override
	public String getText()
	{
		return "To lambda";
	}

	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
