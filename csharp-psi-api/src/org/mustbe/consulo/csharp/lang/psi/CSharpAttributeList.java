package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.psi.DotNetAttributeList;
import consulo.lombok.annotations.ArrayFactoryFields;

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
