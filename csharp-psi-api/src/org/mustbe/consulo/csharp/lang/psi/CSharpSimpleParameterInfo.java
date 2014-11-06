package org.mustbe.consulo.csharp.lang.psi;

import org.consulo.lombok.annotations.ArrayFactoryFields;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

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

	public CSharpSimpleParameterInfo(int index, @Nullable String name, @Nullable PsiElement element, @NotNull DotNetTypeRef typeRef)
	{
		myIndex = index;
		myName = name;
		myElement = element;
		myTypeRef = typeRef;
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
