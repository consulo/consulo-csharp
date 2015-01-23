package org.mustbe.consulo.csharp.ide.codeInsight.actions.expressionActions.lambda;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReturnStatementImpl;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetStatement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
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
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		CSharpDelegateExpressionImpl anonymMethodExpression = PsiTreeUtil.getParentOfType(element, CSharpDelegateExpressionImpl.class);

		assert anonymMethodExpression != null;

		DotNetParameter[] parameters = anonymMethodExpression.getParameters();

		StringBuilder builder = new StringBuilder();

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

		CSharpBlockStatementImpl statement = anonymMethodExpression.getBodyStatement();
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

		anonymMethodExpression.replace(expression);
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		if(element.getNode().getElementType() == CSharpTokens.DELEGATE_KEYWORD)
		{
			CSharpDelegateExpressionImpl anonymMethodExpression = PsiTreeUtil.getParentOfType(element, CSharpDelegateExpressionImpl.class);
			return anonymMethodExpression != null && anonymMethodExpression.toTypeRef(true) != DotNetTypeRef.ERROR_TYPE;
		}
		return false;
	}

	@NotNull
	@Override
	public String getText()
	{
		return "To lambda";
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
