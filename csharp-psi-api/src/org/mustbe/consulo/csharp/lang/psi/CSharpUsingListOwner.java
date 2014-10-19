package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetElement;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public interface CSharpUsingListOwner extends DotNetElement
{
	@Nullable
	CSharpUsingList getUsingList();
}
