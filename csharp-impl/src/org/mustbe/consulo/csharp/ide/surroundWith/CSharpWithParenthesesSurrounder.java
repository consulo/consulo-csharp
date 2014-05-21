package org.mustbe.consulo.csharp.ide.surroundWith;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

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
	public boolean isApplicable(@NotNull PsiElement[] elements)
	{
		return true;
	}

	@Nullable
	@Override
	public TextRange surroundElements(
			@NotNull Project project, @NotNull Editor editor, @NotNull PsiElement[] elements) throws IncorrectOperationException
	{
		val oldExpression = (DotNetExpression) elements[0];

		val newExpression = CSharpFileFactory.createExpression(project, "(" + oldExpression.getText() + ")");

		val replace = oldExpression.replace(newExpression);

		int offset = replace.getTextRange().getEndOffset();
		return new TextRange(offset, offset);
	}
}
