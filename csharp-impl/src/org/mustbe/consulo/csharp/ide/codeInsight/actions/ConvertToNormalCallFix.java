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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class ConvertToNormalCallFix extends PsiElementBaseIntentionAction
{
	public static ConvertToNormalCallFix INSTANCE = new ConvertToNormalCallFix();

	public ConvertToNormalCallFix()
	{
		setText("Convert to normal call");
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
	{
		final CSharpMethodCallExpressionImpl callExpression = PsiTreeUtil.getParentOfType(element, CSharpMethodCallExpressionImpl.class);
		assert callExpression != null;
		CSharpReferenceExpressionImpl referenceExpression = (CSharpReferenceExpressionImpl) callExpression.getCallExpression();

		PsiElement qualifier = referenceExpression.getQualifier();
		assert qualifier != null;

		PsiElement resolve = referenceExpression.resolve();
		if(!CSharpMethodImplUtil.isExtensionWrapper(resolve))
		{
			return;
		}

		val builder = new StringBuilder();

		CSharpTypeDeclaration parentTypeOfMethod = (CSharpTypeDeclaration) resolve.getParent();

		CSharpTypeDeclaration parentTypeOfExpression = PsiTreeUtil.getParentOfType(element, CSharpTypeDeclaration.class);

		if(parentTypeOfMethod != parentTypeOfExpression)
		{
			builder.append(parentTypeOfMethod.getName()).append(".");
		}

		builder.append(referenceExpression.getReferenceName());
		builder.append("(");

		DotNetExpression[] parameterExpressions = callExpression.getParameterExpressions();
		List<PsiElement> elements = new ArrayList<PsiElement>(parameterExpressions.length + 1);
		elements.add(qualifier);
		Collections.addAll(elements, parameterExpressions);

		builder.append(StringUtil.join(elements, new Function<PsiElement, String>()
		{
			@Override
			public String fun(PsiElement element)
			{
				return element.getText();
			}
		}, ", "));
		builder.append(")");

		new WriteCommandAction<Object>(project, getText(), element.getContainingFile())
		{
			@Override
			protected void run(Result<Object> objectResult) throws Throwable
			{
				CSharpExpressionStatementImpl statement = (CSharpExpressionStatementImpl) CSharpFileFactory.createStatement(callExpression
						.getProject(), builder.toString());

				callExpression.replace(statement.getExpression());
			}
		}.execute();
	}

	@Override
	public boolean isAvailable(
			@NotNull Project project, Editor editor, @NotNull PsiElement element)
	{
		return true;
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}
}
