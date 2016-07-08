package org.mustbe.consulo.csharp.lang.psi;

import consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;

/**
 * @author VISTALL
 * @since 17.10.14
 */
@ArrayFactoryFields
public interface CSharpModifierList extends DotNetModifierList
{
	@NotNull
	CSharpAttributeList[] getAttributeLists();
}
