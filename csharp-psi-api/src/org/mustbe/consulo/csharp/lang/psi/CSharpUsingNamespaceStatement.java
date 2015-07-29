package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;

/**
 * @author VISTALL
 * @since 18.10.14
 */
@ArrayFactoryFields
public interface CSharpUsingNamespaceStatement extends CSharpUsingListChild
{
	@Nullable
	String getReferenceText();

	@Nullable
	@RequiredReadAction
	DotNetNamespaceAsElement resolve();

	@Nullable
	DotNetReferenceExpression getNamespaceReference();
}
