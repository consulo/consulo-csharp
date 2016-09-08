package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.lombok.annotations.ArrayFactoryFields;

/**
 * @author VISTALL
 * @since 06.11.14
 */
@ArrayFactoryFields
public interface CSharpSimpleLikeMethod
{
	@NotNull
	@RequiredReadAction
	CSharpSimpleParameterInfo[] getParameterInfos();

	@NotNull
	@RequiredReadAction
	DotNetTypeRef getReturnTypeRef();
}
