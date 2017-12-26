package consulo.csharp.lang.psi.impl.manipulator;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTokensImpl;
import consulo.csharp.lang.psi.impl.source.CSharpConstantExpressionImpl;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
public class CSharpConstantExpressionElementManipulator extends AbstractElementManipulator
{
	@Override
	@RequiredReadAction
	public PsiElement handleContentChange(@NotNull PsiElement element, @NotNull TextRange textRange, String s) throws IncorrectOperationException
	{
		CSharpConstantExpressionImpl constantExpression = (CSharpConstantExpressionImpl) element;
		StringBuilder builder = new StringBuilder();
		IElementType elementType = ((CSharpConstantExpressionImpl) element).getLiteralType();
		if(elementType == CSharpTokens.STRING_LITERAL)
		{
			builder.append("\"");
		}
		else if(elementType == CSharpTokens.VERBATIM_STRING_LITERAL)
		{
			builder.append("@\"");
		}
		else if(elementType == CSharpTokensImpl.INTERPOLATION_STRING_LITERAL)
		{
			builder.append("$\"");
		}

		builder.append(s);

		builder.append("\"");

		return constantExpression.updateText(builder.toString());
	}

	@NotNull
	@Override
	@RequiredReadAction
	public TextRange getRangeInElement(@NotNull PsiElement element)
	{
		return CSharpConstantExpressionImpl.getStringValueTextRange((CSharpConstantExpressionImpl) element);
	}
}
