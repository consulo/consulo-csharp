package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetElement;

/**
 * @author VISTALL
 * @since 19.10.14
 */
public interface CSharpUsingListOwner extends DotNetElement
{
	@NotNull
	@RequiredReadAction
	CSharpUsingListChild[] getUsingStatements();
}
