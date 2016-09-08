package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.Trinity;

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

	@NotNull
	T[] getParameters();

	@NotNull
	DotNetTypeRef getParamsParameterTypeRef();

	@NotNull
	DotNetTypeRef getInnerParamsParameterTypeRef();

	boolean isResolveFromParentTypeRef();

	/**
	 * Return parameter info
	 * 1. Name
	 * 2. TypeRef
	 * 3. Optional flag
	 */
	@NotNull
	@RequiredReadAction
	Trinity<String, DotNetTypeRef, Boolean> getParameterInfo(@NotNull T parameter);
}
