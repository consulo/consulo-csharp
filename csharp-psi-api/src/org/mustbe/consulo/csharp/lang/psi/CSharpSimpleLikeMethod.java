package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 06.11.14
 */
@ArrayFactoryFields
public interface CSharpSimpleLikeMethod
{
	@NotNull
	CSharpSimpleParameterInfo[] getParameterInfos();

	@NotNull
	DotNetTypeRef getReturnTypeRef();
}
