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

package consulo.csharp.ide.codeInsight.actions;

import javax.annotation.Nonnull;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 28.03.2015
 */
public class ConvertNamedToSimpleArgumentFix extends BaseIntentionAction
{
	private SmartPsiElementPointer<CSharpNamedCallArgument> myPointer;

	public ConvertNamedToSimpleArgumentFix(CSharpNamedCallArgument element)
	{
		setText("Convert to simple argument");
		myPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
	}

	@Nonnull
	@Override
	public String getFamilyName()
	{
		return "C#";
	}

	@Override
	public boolean isAvailable(@Nonnull Project project, Editor editor, PsiFile psiFile)
	{
		return myPointer.getElement() != null;
	}

	@Override
	public void invoke(@Nonnull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException
	{
		CSharpNamedCallArgument element = myPointer.getElement();
		if(element == null)
		{
			return;
		}

		DotNetExpression argumentExpression = element.getArgumentExpression();

		assert argumentExpression != null;

		CSharpMethodCallExpressionImpl expression = (CSharpMethodCallExpressionImpl) CSharpFileFactory.createExpression(project, "test(" +
				argumentExpression.getText() + ")");

		CSharpCallArgument callArgument = expression.getCallArguments()[0];

		element.replace(callArgument);
	}
}
