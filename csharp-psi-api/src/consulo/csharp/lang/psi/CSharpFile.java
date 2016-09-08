package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.psi.DotNetFile;
import consulo.dotnet.psi.DotNetQualifiedElement;

/**
 * @author VISTALL
 * @since 18.10.14
 */
public interface CSharpFile extends DotNetFile, CSharpUsingListOwner
{
	@NotNull
	@Override
	DotNetQualifiedElement[] getMembers();
}
