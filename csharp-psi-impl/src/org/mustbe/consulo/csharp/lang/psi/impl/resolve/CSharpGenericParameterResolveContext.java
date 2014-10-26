package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpGenericParameterResolveContext extends CSharpBaseResolveContext<DotNetGenericParameter>
{
	public CSharpGenericParameterResolveContext(@NotNull DotNetGenericParameter element)
	{
		super(element, DotNetGenericExtractor.EMPTY);
	}

	@Override
	public void processMembers(DotNetGenericParameter element, Collector collector)
	{

	}
}
