package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.Nullable;
import consulo.dotnet.psi.DotNetElement;

/**
 * @author VISTALL
 * @since 24.01.15
 */
public interface CSharpArrayInitializerOwner extends DotNetElement
{
	@Nullable
	CSharpArrayInitializerImpl getArrayInitializer();
}
