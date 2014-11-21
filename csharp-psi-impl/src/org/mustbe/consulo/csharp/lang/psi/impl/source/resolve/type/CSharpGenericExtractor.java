package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class CSharpGenericExtractor implements DotNetGenericExtractor
{
	private DotNetGenericParameter[] myGenericParameters;
	private DotNetTypeRef[] myTypeRefs;

	public CSharpGenericExtractor(Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		myGenericParameters = ContainerUtil.toArray(map.keySet(), DotNetGenericParameter.ARRAY_FACTORY);
		myTypeRefs = ContainerUtil.toArray(map.values(), DotNetTypeRef.ARRAY_FACTORY);
	}

	public CSharpGenericExtractor(DotNetGenericParameter[] genericParameters, DotNetTypeRef[] arguments)
	{
		assert genericParameters.length == arguments.length : genericParameters.length + " : " + arguments.length;
		myGenericParameters = genericParameters;
		myTypeRefs = arguments;
	}

	@Nullable
	@Override
	public DotNetTypeRef extract(@NotNull DotNetGenericParameter parameter)
	{
		for(int i = 0; i < myGenericParameters.length; i++)
		{
			DotNetGenericParameter genericParameter = myGenericParameters[i];
			DotNetTypeRef typeRef = myTypeRefs[i];
			if(genericParameter.isEquivalentTo(parameter))
			{
				return typeRef;
			}
		}
		return null;
	}
}
