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

package consulo.csharp.ide.codeInsight.template.postfix;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpParenthesesPostfixTemplate extends PostfixTemplate
{
	public CSharpParenthesesPostfixTemplate()
	{
		super("par", "(expression)");
	}

	@Override
	public boolean isApplicable(@NotNull PsiElement context, @NotNull Document copyDocument, int newOffset)
	{
		DotNetExpression expression = PsiTreeUtil.getParentOfType(context, DotNetExpression.class);
		if(expression == null)
		{
			return false;
		}
		PsiElement parent = expression.getParent();
		if(parent instanceof CSharpNamespaceDeclaration || parent instanceof CSharpUsingListChild)
		{
			return false;
		}
		return true;
	}

	@Override
	public void expand(@NotNull PsiElement context, @NotNull Editor editor)
	{
		DotNetExpression newExpression = CSharpFileFactory.createExpression(context.getProject(), "(" + context.getText() + ")");

		context.replace(newExpression);
	}
}
