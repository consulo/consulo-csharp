package consulo.csharp.lang.psi;

import consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;

/**
 * @author VISTALL
 * @since 18.10.14
 */
@ArrayFactoryFields
public interface CSharpUsingNamespaceStatement extends CSharpUsingListChild
{
	@Nullable
	@RequiredReadAction
	String getReferenceText();

	@Nullable
	@RequiredReadAction
	DotNetNamespaceAsElement resolve();

	@Nullable
	DotNetReferenceExpression getNamespaceReference();
}
