package consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.dotnet.psi.DotNetExpression;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class CSharpLightCallArgument extends LightElement implements CSharpCallArgument
{
	private final DotNetExpression myExpression;

	public CSharpLightCallArgument(@NotNull DotNetExpression expression)
	{
		super(expression.getManager(), expression.getLanguage());
		myExpression = expression;
	}

	@Nullable
	@Override
	public DotNetExpression getArgumentExpression()
	{
		return myExpression;
	}

	@Override
	public String toString()
	{
		return "CSharpLightCallArgument: " + myExpression.getText();
	}
}
