/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.liveTemplates.expression;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.MethodGenerateUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.template.Expression;
import com.intellij.codeInsight.template.ExpressionContext;
import com.intellij.codeInsight.template.Result;
import com.intellij.codeInsight.template.TextResult;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * @author VISTALL
 * @since 06.01.15
 */
public class ReturnStatementExpression extends Expression
{
	@Nullable
	@Override
	public Result calculateResult(ExpressionContext context)
	{
		PsiElement caretElement = getCaretElement(context);
		if(caretElement == null)
		{
			return new TextResult("");
		}

		CSharpSimpleLikeMethodAsElement element = PsiTreeUtil.getParentOfType(caretElement, CSharpSimpleLikeMethodAsElement.class);
		if(element == null)
		{
			return new TextResult("");
		}

		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(element.getReturnTypeRef(), element);
		if(defaultValueForType != null)
		{
			return new TextResult("return " + defaultValueForType + ";");
		}
		return new TextResult("");
	}

	public static PsiElement getCaretElement(ExpressionContext context)
	{
		Project project = context.getProject();

		PsiDocumentManager.getInstance(project).commitAllDocuments();

		Editor editor = context.getEditor();
		if(editor == null)
		{
			return null;
		}
		PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
		return file == null ? null : file.findElementAt(editor.getCaretModel().getOffset());
	}

	@Nullable
	@Override
	public Result calculateQuickResult(ExpressionContext context)
	{
		return calculateResult(context);
	}

	@Nullable
	@Override
	public LookupElement[] calculateLookupItems(ExpressionContext context)
	{
		return LookupElement.EMPTY_ARRAY;
	}
}
