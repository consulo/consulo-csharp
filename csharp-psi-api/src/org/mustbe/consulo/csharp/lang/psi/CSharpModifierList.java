package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.lombok.annotations.ArrayFactoryFields;

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
