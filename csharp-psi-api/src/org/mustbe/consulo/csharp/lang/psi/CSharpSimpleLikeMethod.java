package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public interface CSharpSimpleLikeMethod
{
	@NotNull
	CSharpSimpleParameterInfo[] getParameterInfos();

	@NotNull
	DotNetTypeRef getReturnTypeRef();
}
