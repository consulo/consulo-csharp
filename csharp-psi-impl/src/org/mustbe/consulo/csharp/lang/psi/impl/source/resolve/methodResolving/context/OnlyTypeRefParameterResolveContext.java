package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class OnlyTypeRefParameterResolveContext implements ParameterResolveContext<DotNetTypeRef>
{
	@NotNull
	private final DotNetTypeRef[] myParameterTypeRefs;

	public OnlyTypeRefParameterResolveContext(@NotNull DotNetTypeRef[] parameterTypeRefs)
	{
		myParameterTypeRefs = parameterTypeRefs;
	}

	@Nullable
	@Override
	public DotNetTypeRef getParameterByIndex(int i)
	{
		return ArrayUtil2.safeGet(myParameterTypeRefs, i);
	}

	@Override
	public DotNetTypeRef getParameterByName(@NotNull String name)
	{
		return null;
	}

	@Override
	public int getParametersSize()
	{
		return myParameterTypeRefs.length;
	}

	@Nullable
	@Override
	public DotNetParameter getParamsParameter()
	{
		return null;
	}

	@Override
	public void paramsParameterSpecified()
	{

	}

	@NotNull
	@Override
	public DotNetTypeRef[] getParameters()
	{
		return myParameterTypeRefs;
	}

	@NotNull
	@Override
	public DotNetTypeRef getParamsParameterTypeRef()
	{
		throw new IllegalArgumentException("This method need to never call, check getParamsParameter() for it");
	}

	@NotNull
	@Override
	public DotNetTypeRef getInnerParamsParameterTypeRef()
	{
		throw new IllegalArgumentException("This method need to never call, check getParamsParameter() for it");
	}
}
