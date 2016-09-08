package consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NErrorCallArgument extends NCallArgument
{
	public NErrorCallArgument(@Nullable Object parameterObject)
	{
		super(DotNetTypeRef.ERROR_TYPE, null, parameterObject);
	}

	@Override
	public boolean isValid()
	{
		return false;
	}

	@RequiredReadAction
	@Override
	public int calcValid(@NotNull PsiElement scope)
	{
		return FAIL;
	}
}
