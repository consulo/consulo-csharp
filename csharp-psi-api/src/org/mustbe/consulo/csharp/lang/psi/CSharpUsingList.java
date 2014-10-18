package org.mustbe.consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetElement;

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

	@NotNull
	CSharpUsingListChild[] getStatements();

	void addUsing(@NotNull String qName);
}
