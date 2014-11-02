package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public interface ParameterResolveContext<T>
{
	@Nullable
	T getParameterByIndex(int i);

	@Nullable
	T getParameterByName(@NotNull String name);

	int getParametersSize();

	@Nullable
	DotNetParameter getParamsParameter();

	void paramsParameterSpecified();

	@NotNull
	T[] getParameters();

	@NotNull
	DotNetTypeRef getParamsParameterTypeRef();

	@NotNull
	DotNetTypeRef getInnerParamsParameterTypeRef();
}
