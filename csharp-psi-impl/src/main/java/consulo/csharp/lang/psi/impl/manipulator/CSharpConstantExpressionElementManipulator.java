package consulo.csharp.lang.psi.impl.manipulator;

import javax.annotation.Nonnull;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
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
	public PsiElement handleContentChange(@Nonnull PsiElement element, @Nonnull TextRange textRange, String s) throws IncorrectOperationException
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

	@Nonnull
	@Override
	@RequiredReadAction
	public TextRange getRangeInElement(@Nonnull PsiElement element)
	{
		return CSharpConstantExpressionImpl.getStringValueTextRange((CSharpConstantExpressionImpl) element);
	}
}
