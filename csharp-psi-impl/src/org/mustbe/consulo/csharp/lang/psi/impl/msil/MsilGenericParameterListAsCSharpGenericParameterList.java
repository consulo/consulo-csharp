package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.ArrayList;
import java.util.List;

import consulo.lombok.annotations.Lazy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class MsilGenericParameterListAsCSharpGenericParameterList extends MsilElementWrapper<DotNetGenericParameterList> implements DotNetGenericParameterList
{
	@Nullable
	public static DotNetGenericParameterList build(@NotNull PsiElement parent, @Nullable DotNetGenericParameterList parameterList, GenericParameterContext context)
	{
		if(parameterList == null)
		{
			return null;
		}

		int genericParametersCount = parameterList.getGenericParametersCount();
		context.setGenericParameterCount(genericParametersCount);
		return new MsilGenericParameterListAsCSharpGenericParameterList(parent, parameterList, context);
	}

	private final NotNullLazyValue<DotNetGenericParameter[]> myParametersValue = new NotNullLazyValue<DotNetGenericParameter[]>()
	{
		@NotNull
		@Override
		protected DotNetGenericParameter[] compute()
		{
			DotNetGenericParameter[] oldParameters = myOriginal.getParameters();
			if(oldParameters.length == 0)
			{
				return DotNetGenericParameter.EMPTY_ARRAY;
			}

			List<DotNetGenericParameter> parameters = new ArrayList<DotNetGenericParameter>(oldParameters.length);
			for(int i = 0; i < oldParameters.length; i++)
			{
				if(myGenericParameterContext.isImplicitParameter(i))
				{
					continue;
				}

				parameters.add(new MsilGenericParameterAsCSharpGenericParameter(MsilGenericParameterListAsCSharpGenericParameterList.this, oldParameters[i]));
			}

			return ContainerUtil.toArray(parameters, DotNetGenericParameter.ARRAY_FACTORY);
		}
	};
	private final GenericParameterContext myGenericParameterContext;

	private MsilGenericParameterListAsCSharpGenericParameterList(@NotNull PsiElement parent, DotNetGenericParameterList msilElement, GenericParameterContext genericParameterContext)
	{
		super(parent, msilElement);
		myGenericParameterContext = genericParameterContext;
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
	@Lazy
	public DotNetGenericParameter[] getParameters()
	{
		return myParametersValue.getValue();
	}

	@Override
	public int getGenericParametersCount()
	{
		return getParameters().length;
	}
}
