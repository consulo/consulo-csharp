package consulo.csharp.lang.psi.impl.source.resolve.type;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.util.containers.ContainerUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;

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
		if(genericParameters.length == 0 || genericParameters.length != arguments.length)
		{
			return EMPTY;
		}
		return new CSharpGenericExtractor(genericParameters, arguments);
	}

	private DotNetGenericParameter[] myGenericParameters;
	private DotNetTypeRef[] myTypeRefs;

	private CSharpGenericExtractor(Map<DotNetGenericParameter, DotNetTypeRef> map)
	{
		this(ContainerUtil.toArray(map.keySet(), DotNetGenericParameter.ARRAY_FACTORY), ContainerUtil.toArray(map.values(), DotNetTypeRef.ARRAY_FACTORY));
	}

	private CSharpGenericExtractor(DotNetGenericParameter[] genericParameters, DotNetTypeRef[] arguments)
	{
		myGenericParameters = genericParameters;
		myTypeRefs = arguments;
		assert myGenericParameters.length != 0 : "can't be empty";
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