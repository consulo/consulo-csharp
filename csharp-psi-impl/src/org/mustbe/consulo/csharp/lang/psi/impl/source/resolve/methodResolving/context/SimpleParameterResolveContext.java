package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.openapi.util.Comparing;

/**
 * @author VISTALL
 * @since 06.11.14
 */
public class SimpleParameterResolveContext implements ParameterResolveContext<CSharpSimpleParameterInfo>
{
	@NotNull
	private final CSharpSimpleParameterInfo[] myParameterInfos;

	public SimpleParameterResolveContext(@NotNull CSharpSimpleParameterInfo[] parameterInfos)
	{
		myParameterInfos = parameterInfos;
	}

	@Nullable
	@Override
	public CSharpSimpleParameterInfo getParameterByIndex(int i)
	{
		return ArrayUtil2.safeGet(myParameterInfos, i);
	}

	@Nullable
	@Override
	public CSharpSimpleParameterInfo getParameterByName(@NotNull String name)
	{
		for(CSharpSimpleParameterInfo parameterInfo : myParameterInfos)
		{
			if(Comparing.equal(parameterInfo.getNotNullName(), name))
			{
				return parameterInfo;
			}
		}
		return null;
	}

	@Override
	public int getParametersSize()
	{
		return myParameterInfos.length;
	}

	@Nullable
	@Override
	public DotNetParameter getParamsParameter()
	{
		return null;
	}

	@NotNull
	@Override
	public CSharpSimpleParameterInfo[] getParameters()
	{
		return myParameterInfos;
	}

	@NotNull
	@Override
	public DotNetTypeRef getParamsParameterTypeRef()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@NotNull
	@Override
	public DotNetTypeRef getInnerParamsParameterTypeRef()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	public boolean isResolveFromParentTypeRef()
	{
		return false;
	}
}
