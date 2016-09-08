package org.mustbe.consulo.csharp.ide.codeInsight.actions.expressionActions.lambda;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpBlockStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLambdaExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 01.11.14
 */
public class LambdaToDelegateExpressionFix extends PsiElementBaseIntentionAction
{
	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		CSharpLambdaExpressionImpl lambdaExpression = PsiTreeUtil.getParentOfType(element, CSharpLambdaExpressionImpl.class);
		assert lambdaExpression != null;

		StringBuilder builder = new StringBuilder();

		if(lambdaExpression.hasModifier(CSharpModifier.ASYNC))
		{
			builder.append("async ");
		}

		builder.append("delegate(");

		CSharpLambdaParameter[] parameters = lambdaExpression.getParameters();

		for(int i = 0; i < parameters.length; i++)
		{
			if(i != 0)
			{
				builder.append(", ");
			}
			CSharpLambdaParameter parameter = parameters[i];

			builder.append(CSharpTypeRefPresentationUtil.buildShortText(parameter.toTypeRef(true), lambdaExpression)).append(" ").append(parameter
					.getName());
		}
		builder.append(") { ");

		PsiElement codeBlock = lambdaExpression.getCodeBlock();
		if(codeBlock instanceof DotNetExpression)
		{
			builder.append("return ").append(codeBlock.getText()).append(";");
		}
		else if(codeBlock instanceof CSharpBlockStatementImpl)
		{
			String join = StringUtil.join(((CSharpBlockStatementImpl) codeBlock).getStatements(), new Function<DotNetStatement, String>()
			{
				@Override
				public String fun(DotNetStatement dotNetStatement)
				{
					return dotNetStatement.getText();
				}
			}, "\n");

			builder.append(join);
		}

		builder.append("}");

		DotNetExpression expression = CSharpFileFactory.createExpression(project, builder.toString());

		lambdaExpression.replace(expression);
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		IElementType elementType = PsiUtilCore.getElementType(element);
		if(elementType == CSharpTokens.DARROW)
		{
			CSharpLambdaExpressionImpl lambdaExpression = PsiTreeUtil.getParentOfType(element, CSharpLambdaExpressionImpl.class);
			return lambdaExpression != null && lambdaExpression.toTypeRef(true) != DotNetTypeRef.ERROR_TYPE;
		}
		return false;
	}

	@NotNull
	@Override
	public String getText()
	{
		return "To delegate";
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
