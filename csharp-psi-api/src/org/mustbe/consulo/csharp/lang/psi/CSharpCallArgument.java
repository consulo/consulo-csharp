package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.Nullable;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetExpression;
import consulo.lombok.annotations.ArrayFactoryFields;

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
