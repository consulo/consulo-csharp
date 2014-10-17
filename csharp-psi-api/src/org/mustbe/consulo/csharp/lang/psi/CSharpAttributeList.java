package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeList;

/**
 * @author VISTALL
 * @since 17.10.14
 */
@ArrayFactoryFields
public interface CSharpAttributeList extends DotNetAttributeList
{
	@NotNull
	@Override
	CSharpAttribute[] getAttributes();
}
