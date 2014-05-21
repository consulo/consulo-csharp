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

package org.mustbe.consulo.csharp.ide.reflactoring.introduceVariable;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpExpressionStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNativeTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class CSharpIntroduceVariableHandler implements RefactoringActionHandler
{
	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext)
	{
		int offset = editor.getCaretModel().getOffset();

		PsiElement elementAt = file.findElementAt(offset);
		if(elementAt == null)
		{
			return;
		}

		DotNetExpression expression = PsiTreeUtil.getParentOfType(elementAt, DotNetExpression.class);
		if(expression == null)
		{
			PsiElement parent = elementAt.getParent();
			if(parent instanceof CSharpExpressionStatementImpl)
			{
				expression = ((CSharpExpressionStatementImpl) parent).getExpression();
			}

			if(expression == null)
			{
				return;
			}
		}

		DotNetTypeRef dotNetTypeRef = expression.toTypeRef(true);
		if(dotNetTypeRef == CSharpNativeTypeRef.VOID)
		{
			CommonRefactoringUtil.showErrorHint(project, editor, "Expression type is 'void'", RefactoringBundle.message("introduce.variable.title"),
					"IntroduceVariable");
			return;
		}

		if(expression.getParent() instanceof CSharpExpressionStatementImpl)
		{
			StringBuilder builder = new StringBuilder();

			builder.append(dotNetTypeRef.getPresentableText()).append(" b = ").append(expression.getText()).append(";");

			val localVariableStatement = CSharpFileFactory.createStatement(project, builder.toString());

			val temp = expression;
			new WriteCommandAction<Object>(project, RefactoringBundle.message("introduce.variable.title"), file)
			{
				@Override
				protected void run(Result<Object> objectResult) throws Throwable
				{
					temp.getParent().replace(localVariableStatement);
				}
			}.execute();
		}
	}

	@Override
	public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext)
	{

	}
}
