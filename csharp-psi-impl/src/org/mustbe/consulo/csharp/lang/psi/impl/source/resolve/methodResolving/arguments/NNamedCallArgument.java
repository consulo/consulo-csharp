package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NNamedCallArgument extends NCallArgument
{
	private String myName;

	public NNamedCallArgument(@NotNull DotNetTypeRef typeRef, @Nullable CSharpCallArgument callArgument,
			@Nullable Object parameter, @NotNull String name)
	{
		super(typeRef, callArgument, parameter);
		myName = name;
	}

	public String getName()
	{
		return myName;
	}
}
