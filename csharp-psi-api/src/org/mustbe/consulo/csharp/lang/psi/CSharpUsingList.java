package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.resolve.DotNetNamespaceAsElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 18.10.14
 */
public interface CSharpUsingList extends DotNetElement
{
	@NotNull
	CSharpTypeDefStatement[] getTypeDefs();

	@NotNull
	CSharpUsingNamespaceStatement[] getUsingDirectives();

	CSharpUsingTypeStatement[] getUsingTypeDirectives();

	@NotNull
	DotNetNamespaceAsElement[] getUsingNamespaces();

	@NotNull
	DotNetTypeRef[] getUsingTypeRefs();

	@NotNull
	CSharpUsingListChild[] getStatements();

	void addUsing(@NotNull String qName);
}
