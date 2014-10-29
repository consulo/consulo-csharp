package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;

/**
 * @author VISTALL
 * @since 15.09.14
 */
@ArrayFactoryFields
public interface CSharpCallArgument extends DotNetElement
{
	@Nullable
	DotNetExpression getArgumentExpression();
}
