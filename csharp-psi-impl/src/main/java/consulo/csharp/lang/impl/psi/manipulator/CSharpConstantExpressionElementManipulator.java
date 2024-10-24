package consulo.csharp.lang.impl.psi.manipulator;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.impl.psi.CSharpTokensImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConditionalExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpConstantExpressionImpl;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.document.util.TextRange;
import consulo.language.ast.IElementType;
import consulo.language.psi.AbstractElementManipulator;
import consulo.language.util.IncorrectOperationException;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 07-Jan-17
 */
@ExtensionImpl
public class CSharpConstantExpressionElementManipulator extends AbstractElementManipulator<CSharpConstantExpressionImpl>
{
	@Override
	@RequiredReadAction
	public CSharpConstantExpressionImpl handleContentChange(@Nonnull CSharpConstantExpressionImpl element, @Nonnull TextRange textRange, String s) throws IncorrectOperationException
	{
		StringBuilder builder = new StringBuilder();
		IElementType elementType = element.getLiteralType();
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

		return (CSharpConstantExpressionImpl) element.updateText(builder.toString());
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public TextRange getRangeInElement(@Nonnull CSharpConstantExpressionImpl element)
	{
		return CSharpConstantExpressionImpl.getStringValueTextRange(element);
	}

	@Nonnull
	@Override
	public Class getElementClass()
	{
		return CSharpConditionalExpressionImpl.class;
	}
}
