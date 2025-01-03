/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.impl.psi.msil;

import consulo.application.util.NotNullLazyValue;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.language.psi.PsiElement;
import consulo.util.collection.ContainerUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class MsilGenericParameterListAsCSharpGenericParameterList extends MsilElementWrapper<DotNetGenericParameterList> implements DotNetGenericParameterList
{
	@Nullable
	public static DotNetGenericParameterList build(@Nonnull PsiElement parent, @Nullable DotNetGenericParameterList parameterList, GenericParameterContext context)
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
		@Nonnull
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

	private MsilGenericParameterListAsCSharpGenericParameterList(@Nonnull PsiElement parent, DotNetGenericParameterList msilElement, GenericParameterContext genericParameterContext)
	{
		super(parent, msilElement);
		myGenericParameterContext = genericParameterContext;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameterList(this);
	}

	@Override
	public String toString()
	{
		return "MsilGenericParameterListAsCSharpGenericParameterList";
	}

	@Nonnull
	@Override
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
