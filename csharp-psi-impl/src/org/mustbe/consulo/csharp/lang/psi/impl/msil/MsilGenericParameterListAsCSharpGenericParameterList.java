package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterList;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class MsilGenericParameterListAsCSharpGenericParameterList extends MsilElementWrapper<DotNetGenericParameterList> implements
		DotNetGenericParameterList
{
	public MsilGenericParameterListAsCSharpGenericParameterList(@Nullable PsiElement parent, DotNetGenericParameterList msilElement)
	{
		super(parent, msilElement);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameterList(this);
	}

	@Override
	public String toString()
	{
		return "MsilGenericParameterListAsCSharpGenericParameterList";
	}

	@NotNull
	@Override
	@LazyInstance
	public DotNetGenericParameter[] getParameters()
	{
		DotNetGenericParameter[] oldParameters = myOriginal.getParameters();
		if(oldParameters.length == 0)
		{
			return DotNetGenericParameter.EMPTY_ARRAY;
		}
		MsilGenericParameterAsCSharpGenericParameter[] parameters = new MsilGenericParameterAsCSharpGenericParameter[oldParameters.length];
		for(int i = 0; i < oldParameters.length; i++)
		{
			parameters[i] = new MsilGenericParameterAsCSharpGenericParameter(MsilGenericParameterListAsCSharpGenericParameterList.this,
					oldParameters[i]);
		}
		return parameters;
	}

	@Override
	public int getGenericParametersCount()
	{
		return myOriginal.getGenericParametersCount();
	}
}
