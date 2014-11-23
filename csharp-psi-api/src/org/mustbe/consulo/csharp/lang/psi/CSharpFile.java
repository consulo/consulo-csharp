package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetFile;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;

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
