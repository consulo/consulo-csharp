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

package consulo.csharp.impl.ide.codeInsight.actions.expressionActions.lambda;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.psi.CSharpFileFactory;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpBlockStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.editor.intention.IntentionMetaData;
import consulo.language.editor.intention.PsiElementBaseIntentionAction;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.lang.StringUtil;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 01.11.14
 */
@ExtensionImpl
@IntentionMetaData(ignoreId = "csharp.lambda.to.delegate", categories = "C#", fileExtensions = "cs")
public class LambdaToDelegateExpressionFix extends PsiElementBaseIntentionAction
{
	@Override
	@RequiredReadAction
	public void invoke(@Nonnull Project project, Editor editor, @Nonnull PsiElement element) throws IncorrectOperationException
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

			builder.append(CSharpTypeRefPresentationUtil.buildShortText(parameter.toTypeRef(true))).append(" ").append(parameter.getName());
		}
		builder.append(") { ");

		PsiElement codeBlock = lambdaExpression.getCodeBlock().getElement();
		if(codeBlock instanceof DotNetExpression)
		{
			builder.append("return ").append(codeBlock.getText()).append(";");
		}
		else if(codeBlock instanceof CSharpBlockStatementImpl)
		{
			String join = StringUtil.join(((CSharpBlockStatementImpl) codeBlock).getStatements(), PsiElement::getText, "\n");

			builder.append(join);
		}

		builder.append("}");

		DotNetExpression expression = CSharpFileFactory.createExpression(project, builder.toString());

		lambdaExpression.replace(expression);
	}

	@Override
	@RequiredReadAction
	public boolean isAvailable(@Nonnull Project project, Editor editor, @Nonnull PsiElement element)
	{
		IElementType elementType = PsiUtilCore.getElementType(element);
		if(elementType == CSharpTokens.DARROW)
		{
			CSharpLambdaExpressionImpl lambdaExpression = PsiTreeUtil.getParentOfType(element, CSharpLambdaExpressionImpl.class);
			return lambdaExpression != null && lambdaExpression.toTypeRef(true) != DotNetTypeRef.ERROR_TYPE;
		}
		return false;
	}

	@Nonnull
	@Override
	public String getText()
	{
		return "To delegate";
	}

	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
