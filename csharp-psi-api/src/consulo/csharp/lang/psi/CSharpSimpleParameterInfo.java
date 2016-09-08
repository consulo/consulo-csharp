package consulo.csharp.lang.psi;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.lombok.annotations.ArrayFactoryFields;

/**
 * @author VISTALL
 * @since 06.11.14
 */
@ArrayFactoryFields
public class CSharpSimpleParameterInfo
{
	@NotNull
	public static DotNetTypeRef[] toTypeRefs(@NotNull CSharpSimpleParameterInfo[] parameterInfos)
	{
		if(parameterInfos.length == 0)
		{
			return DotNetTypeRef.EMPTY_ARRAY;
		}
		DotNetTypeRef[] typeRefs = new DotNetTypeRef[parameterInfos.length];
		for(int i = 0; i < parameterInfos.length; i++)
		{
			CSharpSimpleParameterInfo parameterInfo = parameterInfos[i];
			typeRefs[i] = parameterInfo.getTypeRef();
		}
		return typeRefs;
	}

	private int myIndex;
	private String myName;
	@Nullable
	private final PsiElement myElement;
	private DotNetTypeRef myTypeRef;

	private boolean myOptional;

	@RequiredReadAction
	public CSharpSimpleParameterInfo(int index, @NotNull DotNetParameter parameter, @NotNull DotNetTypeRef typeRef)
	{
		myIndex = index;
		myName = parameter.getName();
		myElement = parameter;
		myTypeRef = typeRef;
		myOptional = parameter.hasModifier(CSharpModifier.OPTIONAL);
	}

	@RequiredReadAction
	public CSharpSimpleParameterInfo(int index, @Nullable String name, @Nullable PsiElement element, @NotNull DotNetTypeRef typeRef)
	{
		myIndex = index;
		myName = name;
		myElement = element;
		myTypeRef = typeRef;
	}

	public boolean isOptional()
	{
		return myOptional;
	}

	@Nullable
	public PsiElement getElement()
	{
		return myElement;
	}

	public int getIndex()
	{
		return myIndex;
	}

	@Nullable
	public String getName()
	{
		return myName;
	}

	@NotNull
	public String getNotNullName()
	{
		return myName == null ? "p" + myIndex : myName;
	}

	@NotNull
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}
}
