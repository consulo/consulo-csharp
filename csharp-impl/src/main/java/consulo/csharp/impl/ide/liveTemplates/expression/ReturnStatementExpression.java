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

package consulo.csharp.impl.ide.liveTemplates.expression;

import javax.annotation.Nullable;
import consulo.csharp.impl.ide.codeInsight.actions.MethodGenerateUtil;
import consulo.csharp.lang.psi.CSharpSimpleLikeMethodAsElement;
import consulo.language.editor.completion.lookup.LookupElement;
import consulo.language.editor.template.ExpressionContext;
import consulo.language.editor.template.TextResult;
import consulo.codeEditor.Editor;
import consulo.language.psi.PsiDocumentManager;
import consulo.language.psi.PsiElement;
import consulo.language.editor.template.Expression;
import consulo.language.editor.template.Result;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.project.Project;

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

		String defaultValueForType = MethodGenerateUtil.getDefaultValueForType(element.getReturnTypeRef());
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
