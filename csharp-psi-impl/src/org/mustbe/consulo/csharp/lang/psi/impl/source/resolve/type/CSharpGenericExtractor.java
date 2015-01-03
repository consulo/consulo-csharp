package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class CSharpGenericExtractor implements DotNetGenericExtractor
{
	@NotNull
	public static DotNetGenericExtractor create(@NotNull Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		if(map.isEmpty())
		{
			return EMPTY;
		}
		return new CSharpGenericExtractor(map);
	}

	@NotNull
	public static DotNetGenericExtractor create(@NotNull DotNetGenericParameter[] genericParameters, @NotNull DotNetTypeRef[] arguments)
	{
		assert genericParameters.length == arguments.length : genericParameters.length + " : " + arguments.length;
		if(genericParameters.length == 0)
		{
			return EMPTY;
		}
		return new CSharpGenericExtractor(genericParameters, arguments);
	}

	private DotNetGenericParameter[] myGenericParameters;
	private DotNetTypeRef[] myTypeRefs;

	private CSharpGenericExtractor(Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		myGenericParameters = ContainerUtil.toArray(map.keySet(), DotNetGenericParameter.ARRAY_FACTORY);
		myTypeRefs = ContainerUtil.toArray(map.values(), DotNetTypeRef.ARRAY_FACTORY);
	}

	private CSharpGenericExtractor(DotNetGenericParameter[] genericParameters, DotNetTypeRef[] arguments)
	{
		myGenericParameters = genericParameters;
		myTypeRefs = arguments;
	}

	@Nullable
	@Override
	public DotNetTypeRef extract(@NotNull DotNetGenericParameter parameter)
	{
		int index = -1;
		for(int i = 0; i < myGenericParameters.length; i++)
		{
			DotNetGenericParameter genericParameter = myGenericParameters[i];
			if(genericParameter.isEquivalentTo(parameter))
			{
				index = i;
				break;
			}
		}

		if(index == -1)
		{
			return null;
		}

		return ArrayUtil2.safeGet(myTypeRefs, index);
	}
}
