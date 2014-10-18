package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;

/**
 * @author VISTALL
 * @since 18.10.14
 */
public interface CSharpUsingNamespaceStatement extends CSharpUsingListChild
{
	@Nullable
	String getReferenceText();

	@Nullable
	DotNetNamespaceAsElement resolve();

	@Nullable
	DotNetReferenceExpression getNamespaceReference();
}
