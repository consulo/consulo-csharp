package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NParamsCallArgument extends NCallArgument
{
	@NotNull
	private final List<CSharpCallArgument> myCallArguments;

	public NParamsCallArgument(@NotNull DotNetTypeRef typeRef, @NotNull List<CSharpCallArgument> callArguments, @Nullable DotNetParameter parameter)
	{
		super(typeRef, null, parameter);
		myCallArguments = callArguments;
	}

	@NotNull
	@Override
	public Collection<CSharpCallArgument> getCallArguments()
	{
		return myCallArguments;
	}
}
