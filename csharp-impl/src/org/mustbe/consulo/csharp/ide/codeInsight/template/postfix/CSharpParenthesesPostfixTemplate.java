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

package org.mustbe.consulo.csharp.ide.codeInsight.template.postfix;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.val;

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

		DotNetExpression parentOfType = PsiTreeUtil.getParentOfType(context, DotNetExpression.class);

		return parentOfType != null;
	}

	@Override
	public void expand(@NotNull PsiElement context, @NotNull Editor editor)
	{
		val newExpression = CSharpFileFactory.createExpression(context.getProject(), "(" + context.getText() + ")");

		context.replace(newExpression);
	}
}
