package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public class CSharpTypeResolveContext extends CSharpBaseResolveContext<CSharpTypeDeclaration>
{
	public CSharpTypeResolveContext(@NotNull CSharpTypeDeclaration element, @NotNull DotNetGenericExtractor genericExtractor)
	{
		super(element, genericExtractor);
	}

	@Override
	public void processMembers(CSharpTypeDeclaration element, Collector collector)
	{
		DotNetNamedElement[] members = element.getMembers();
		for(DotNetNamedElement member : members)
		{
			member.accept(collector);
		}
	}
}
