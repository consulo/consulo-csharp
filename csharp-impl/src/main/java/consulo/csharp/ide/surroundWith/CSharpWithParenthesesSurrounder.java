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

package consulo.csharp.ide.surroundWith;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 17.05.14
 */
public class CSharpWithParenthesesSurrounder implements Surrounder
{
	@Override
	public String getTemplateDescription()
	{
		return "(expression)";
	}

	@Override
	public boolean isApplicable(@Nonnull PsiElement[] elements)
	{
		return true;
	}

	@Nullable
	@Override
	public TextRange surroundElements(
			@Nonnull Project project, @Nonnull Editor editor, @Nonnull PsiElement[] elements) throws IncorrectOperationException
	{
		DotNetExpression oldExpression = (DotNetExpression) elements[0];

		DotNetExpression newExpression = CSharpFileFactory.createExpression(project, "(" + oldExpression.getText() + ")");

		PsiElement replace = oldExpression.replace(newExpression);

		int offset = replace.getTextRange().getEndOffset();
		return new TextRange(offset, offset);
	}
}
